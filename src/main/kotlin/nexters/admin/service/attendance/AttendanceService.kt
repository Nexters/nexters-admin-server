package nexters.admin.service.attendance

import nexters.admin.controller.auth.LoggedInMemberRequest
import nexters.admin.domain.attendance.Attendance
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.generation.GenerationStatus
import nexters.admin.domain.session.Session
import nexters.admin.exception.BadRequestException
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.AttendanceRepository
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.GenerationRepository
import nexters.admin.repository.MemberRepository
import nexters.admin.repository.QrCodeRepository
import nexters.admin.repository.SessionRepository
import nexters.admin.repository.findAllPendingAttendanceOf
import nexters.admin.repository.findGenerationAttendancesIn
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Transactional
@Service
class AttendanceService(
        private val attendanceRepository: AttendanceRepository,
        private val memberRepository: MemberRepository,
        private val generationMemberRepository: GenerationMemberRepository,
        private val qrCodeRepository: QrCodeRepository,
        private val sessionRepository: SessionRepository,
        private val generationRepository: GenerationRepository,
) {
    @Transactional(readOnly = true)
    fun getAttendanceProfile(loggedInMemberRequest: LoggedInMemberRequest): FindAttendanceProfileResponse {
        val loggedInMember = memberRepository.findByEmail(loggedInMemberRequest.email)
                ?: throw NotFoundException.memberNotFound()
        val latestOngoingGeneration = findLatestOngoingGeneration()
        val generationMember = generationMemberRepository.findByGenerationAndMemberId(latestOngoingGeneration, loggedInMember.id)
                ?: return FindAttendanceProfileResponse.of()

        val statuses = getValidAttendanceStatuses()

        val attendances = attendanceRepository.findGenerationAttendancesIn(generationMember.id, statuses)
        val sessions = sessionRepository.findAllByGeneration(latestOngoingGeneration)

        val sessionToAttendance = getWeekSortedSessionToAttendance(attendances, sessions)
        return FindAttendanceProfileResponse.of(generationMember, sessionToAttendance)
    }

    private fun getValidAttendanceStatuses(): List<AttendanceStatus> {
        val statuses = AttendanceStatus.values().toMutableList()
        statuses.remove(AttendanceStatus.PENDING)
        return statuses;
    }

    private fun getWeekSortedSessionToAttendance(
            attendances: List<Attendance>,
            sessions: List<Session>,
    ): SortedMap<Session, Attendance> {
        val sessionToAttendance = attendances.associateBy {
            sessions.findLast { session -> session.id == it.sessionId }
                    ?: throw NotFoundException.sessionNotFound()
        }
        return sessionToAttendance.toSortedMap(compareBy<Session> { it.week }.reversed())
    }

    // TODO: 현재 상태가 무엇이든 QR 코드를 찍었으면 출석/지각으로 덮어써지는 구조. PENDING일 때만 변하도록 해야 하는가?
    fun attendWithQrCode(loggedInMemberRequest: LoggedInMemberRequest, qrCode: String) {
        val loggedInMember = memberRepository.findByEmail(loggedInMemberRequest.email)
                ?: throw NotFoundException.memberNotFound()
        val validCode = qrCodeRepository.findCurrentValidCode()
                ?: throw BadRequestException.attendanceNotStarted()
        if (!validCode.isSameValue(qrCode)) {
            throw BadRequestException.wrongQrCodeValue()
        }
        val generationMember = generationMemberRepository.findByGenerationAndMemberId(findLatestOngoingGeneration(), loggedInMember.id)
                ?: throw BadRequestException.notGenerationMember()
        val attendance = attendanceRepository.findByGenerationMemberIdAndSessionId(generationMember.id, validCode.sessionId)
                ?: throw NotFoundException.sessionNotFound()
        attendance.updateStatusByQr(validCode.type)
        updateGenerationMemberScore(attendance.generationMemberId)
    }

    fun addExtraAttendanceScoreByAdministrator(attendanceId: Long, extraScoreChange: Int, extraScoreNote: String?) {
        val attendance = attendanceRepository.findByIdOrNull(attendanceId)
                ?: throw NotFoundException.attendanceNotFound()
        attendance.addExtraScore(extraScoreChange, extraScoreNote)
        updateGenerationMemberScore(attendance.generationMemberId)
    }

    fun updateAttendanceStatusByAdministrator(attendanceId: Long, attendanceStatus: String, note: String?) {
        val attendance = attendanceRepository.findByIdOrNull(attendanceId)
                ?: throw NotFoundException.attendanceNotFound()
        attendance.updateStatusByAdmin(AttendanceStatus.from(attendanceStatus), note)
        updateGenerationMemberScore(attendance.generationMemberId)
    }

    fun endAttendance() {
        val activeSessionId = qrCodeRepository.getCurrentSessionId()
                ?: throw BadRequestException.attendanceNotStarted()
        qrCodeRepository.clear()

        val activeSession = (sessionRepository.findByIdOrNull(activeSessionId)
                ?: throw NotFoundException.sessionNotFound())
        activeSession.updateEndAttendTime(LocalDateTime.now())

        val pendingAttendances = attendanceRepository.findAllPendingAttendanceOf(activeSessionId)
        pendingAttendances.forEach {
            it.updateStatusByQr(AttendanceStatus.UNAUTHORIZED_ABSENCE)
            updateGenerationMemberScore(it.generationMemberId)
        }
    }

    private fun findLatestOngoingGeneration(): Int {
        return generationRepository.findAll()
                .filter { it.status == GenerationStatus.DURING_ACTIVITY }
                .maxByOrNull { it.generation }
                ?.generation
                ?: throw NotFoundException.generationNotFound()
    }

    private fun updateGenerationMemberScore(generationMemberId: Long) {
        val generationMember = generationMemberRepository.findByIdOrNull(generationMemberId)
                ?: throw NotFoundException.generationMemberNotFound()
        val currentGenerationScoreChanges = attendanceRepository.findAllByGenerationMemberId(generationMemberId)
                .map { it.scoreChanged }
        generationMember.updateScoreByChanges(currentGenerationScoreChanges)
    }

    @Transactional(readOnly = true)
    fun findAttendancesBySessionId(sessionId: Long): AttendanceSessionResponses {
        val session = (sessionRepository.findByIdOrNull(sessionId)
                ?: throw NotFoundException.sessionNotFound())
        val attendances = attendanceRepository.findAllBySessionId(sessionId)
        val attended = findAttendedMembers(attendances).size
        val tardy = findTardyMembers(attendances).size
        val absence = findAbsenceMembers(attendances).size
        return AttendanceSessionResponses(
                week = session.week,
                sessionDate = session.sessionDate,
                attended = attended,
                tardy = tardy,
                absence = absence,
                data = attendances.map { findAttendanceBySession(it) }
        )
    }

    private fun findAttendanceBySession(it: Attendance): AttendanceSessionResponse {
        val generationMember = generationMemberRepository.findByIdOrNull(it.generationMemberId)
                ?: throw NotFoundException.generationMemberNotFound()
        val member = memberRepository.findByIdOrNull(generationMember.memberId)
                ?: throw NotFoundException.memberNotFound()
        val initialGeneration = generationMemberRepository.findTopByMemberIdOrderByGenerationAsc(member.id)
                ?.generation
                ?: throw NotFoundException.generationNotFound()
        return AttendanceSessionResponse(
                name = member.name,
                attendanceId = it.id,
                position = generationMember.position?.value,
                subPosition = generationMember.subPosition?.value,
                initialGeneration = initialGeneration,
                scoreChanged = it.scoreChanged,
                score = generationMember.score,
                attendanceStatus = it.attendanceStatus.value,
                extraScoreNote = it.extraScoreNote,
                note = it.note
        )
    }

    private fun findAttendedMembers(attendances: List<Attendance>) =
            attendances.filter { it.attendanceStatus == AttendanceStatus.ATTENDED }

    private fun findTardyMembers(attendances: List<Attendance>) =
            attendances.filter { it.attendanceStatus == AttendanceStatus.TARDY }

    private fun findAbsenceMembers(attendances: List<Attendance>) =
            attendances.filter {
                it.attendanceStatus == AttendanceStatus.UNAUTHORIZED_ABSENCE ||
                        it.attendanceStatus == AttendanceStatus.AUTHORIZED_ABSENCE
            }
}
