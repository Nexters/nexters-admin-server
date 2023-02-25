package nexters.admin.service.attendance

import nexters.admin.domain.attendance.Attendance
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.generation.GenerationStatus
import nexters.admin.domain.session.Session
import nexters.admin.domain.user.member.Member
import nexters.admin.exception.BadRequestException
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.AttendanceRepository
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.GenerationRepository
import nexters.admin.repository.QrCodeRepository
import nexters.admin.repository.SessionRepository
import nexters.admin.repository.findAllPendingAttendanceOf
import nexters.admin.repository.findGenerationAttendancesIn
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
@Service
class AttendanceService(
        private val attendanceRepository: AttendanceRepository,
        private val generationMemberRepository: GenerationMemberRepository,
        private val qrCodeRepository: QrCodeRepository,
        private val sessionRepository: SessionRepository,
        private val generationRepository: GenerationRepository,
) {
    @Transactional(readOnly = true)
    fun getAttendanceProfile(loggedInMember: Member): FindAttendanceProfileResponse {
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
    fun attendWithQrCode(loggedInMember: Member, qrCode: String) {
        val validCode = qrCodeRepository.findCurrentValidCode()
                ?: throw BadRequestException.attendanceNotStarted()
        if (!validCode.isSameValue(qrCode)) {
            throw BadRequestException.wrongQrCodeValue()
        }
        val generationMember = generationMemberRepository.findByGenerationAndMemberId(findLatestOngoingGeneration(), loggedInMember.id)
                ?: throw BadRequestException.notGenerationMember()
        val attendance = attendanceRepository.findByGenerationMemberIdAndSessionId(generationMember.id, validCode.sessionId)
                ?: throw NotFoundException.sessionNotFound()
        attendance.updateStatus(validCode.type)
    }

    fun endAttendance() {
        val activeSessionId = qrCodeRepository.getCurrentSessionId()
                ?: throw BadRequestException.attendanceNotStarted()
        qrCodeRepository.clear()

        val pendingAttendances = attendanceRepository.findAllPendingAttendanceOf(activeSessionId)
        pendingAttendances.forEach {
            it.updateStatus(AttendanceStatus.UNAUTHORIZED_ABSENCE)
        }
    }

    private fun findLatestOngoingGeneration(): Int {
        return generationRepository.findAll()
                .filter { it.status == GenerationStatus.DURING_ACTIVITY }
                .maxByOrNull { it.generation }
                ?.generation
                ?: throw NotFoundException.generationNotFound()
    }
}
