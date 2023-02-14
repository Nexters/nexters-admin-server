package nexters.admin.service.attendance

import nexters.admin.domain.attendance.Attendance
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.session.Session
import nexters.admin.domain.user.member.Member
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.AttendanceRepository
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.SessionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
@Service
class AttendanceService(
        private val attendanceRepository: AttendanceRepository,
        private val generationMemberRepository: GenerationMemberRepository,
        private val sessionRepository: SessionRepository
) {
    // TODO: 현재 진행중인 기수를 알 방법이 없음!!! 일단 22기로 박고 진행
    @Transactional(readOnly = true)
    fun getAttendanceProfile(loggedInMember: Member): FindAttendanceProfileResponse {
        val ongoingGeneration = 22;

        val generationMember =
                generationMemberRepository.findByGenerationAndMemberId(ongoingGeneration, loggedInMember.id)
                        ?: return FindAttendanceProfileResponse.of()

        val statuses = getValidAttendanceStatuses()

        val attendances = attendanceRepository.findAllByGenerationMemberIdAndAttendanceStatusIn(generationMember.id, statuses)
        val sessions = sessionRepository.findAllByGeneration(ongoingGeneration)

        val sessionToAttendance = getWeekSortedSessionToAttendance(attendances, sessions)
        return FindAttendanceProfileResponse.of(generationMember, sessionToAttendance)
    }

    private fun getWeekSortedSessionToAttendance(
            attendances: List<Attendance>,
            sessions: List<Session>): SortedMap<Session, Attendance> {
        val sessionToAttendance = attendances.associateBy {
            sessions.findLast { session -> session.id == it.sessionId }
                    ?: throw NotFoundException.sessionNotFound()
        }
        return sessionToAttendance.toSortedMap(compareBy<Session> { it.week }.reversed())
    }

    private fun getValidAttendanceStatuses(): List<AttendanceStatus> {
        val statuses = AttendanceStatus.values().toMutableList()
        statuses.remove(AttendanceStatus.PENDING)
        return statuses;
    }
}
