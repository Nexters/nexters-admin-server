package nexters.admin.service.attendance

import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.session.Session
import nexters.admin.domain.user.member.Member
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.AttendanceRepository
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.SessionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class AttendanceService(
        private val attendanceRepository: AttendanceRepository,
        private val generationMemberRepository: GenerationMemberRepository,
        private val sessionRepository: SessionRepository
) {
    // TODO: 현재 진행중인 기수를 알 방법이 없음!!! 일단 22기로 박고 진행
    fun getAttendanceProfile(loggedInMember: Member): FindAttendanceProfileResponse {
        val ongoingGeneration = 22;

        val generationMember =
                generationMemberRepository.findByGenerationAndMemberId(ongoingGeneration, loggedInMember.id)
                        ?: return FindAttendanceProfileResponse.of()

        val statuses = AttendanceStatus.values().toMutableList()
        statuses.remove(AttendanceStatus.PENDING)

        val attendances = attendanceRepository.findAllByGenerationMemberIdAndAttendanceStatusIn(generationMember.id, statuses)
        val sessions = sessionRepository.findAllByGeneration(ongoingGeneration)

        val sessionToAttendance = attendances.associateBy {
            sessions.findLast { session -> session.id == it.sessionId }
                    ?: throw NotFoundException.sessionNotFound()
        }
        val weekSortedSessionToAttendance = sessionToAttendance.toSortedMap(compareBy<Session> { it.week }.reversed())
        return FindAttendanceProfileResponse.of(generationMember, weekSortedSessionToAttendance)
    }
}
