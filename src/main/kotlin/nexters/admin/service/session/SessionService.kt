package nexters.admin.service.session

import nexters.admin.controller.session.CreateSessionRequest
import nexters.admin.controller.session.UpdateSessionRequest
import nexters.admin.domain.attendance.Attendance
import nexters.admin.domain.attendance.AttendanceStatus.PENDING
import nexters.admin.domain.session.Session
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.AttendanceRepository
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.MemberRepository
import nexters.admin.repository.SessionRepository
import nexters.admin.repository.findUpcomingSession
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

const val CURRENT_ONGOING_GENERATION = 22

@Transactional
@Service
class SessionService(
        private val memberRepository: MemberRepository,
        private val sessionRepository: SessionRepository,
        private val generationMemberRepository: GenerationMemberRepository,
        private val attendanceRepository: AttendanceRepository,
) {

    @Transactional(readOnly = true)
    fun findSessionByGeneration(generation: Int): FindSessionResponses {
        return FindSessionResponses(
                sessionRepository.findAllByGeneration(generation)
                        .map { FindSessionResponse.from(it) }
                        .sortedByDescending { it.sessionDate }
        )
    }

    fun createSession(request: CreateSessionRequest): Long {
        val savedSession = sessionRepository.save(
                Session(
                        title = request.title,
                        description = request.description,
                        generation = request.generation,
                        sessionDate = request.sessionDate,
                        week = request.week,
                )
        )
        val generationMembers = generationMemberRepository.findAllByGeneration(request.generation)
        val attendances = generationMembers.map {
            Attendance(
                    generationMemberId = it.id,
                    sessionId = savedSession.id,
                    attendanceStatus = PENDING
            )
        }
        attendanceRepository.saveAll(attendances)

        return savedSession.id
    }

    @Transactional(readOnly = true)
    fun findSession(id: Long): FindSessionResponse {
        return sessionRepository.findByIdOrNull(id)
                ?.let { FindSessionResponse.from(it) }
                ?: throw NotFoundException.sessionNotFound()
    }

    fun updateSession(sessionId: Long, request: UpdateSessionRequest) {
        val session = sessionRepository.findByIdOrNull(sessionId)
                ?: throw NotFoundException.sessionNotFound()

        session.apply {
            title = request.title
            description = request.description
            generation = request.generation
            sessionDate = request.sessionDate
            week = request.week
        }

        sessionRepository.save(session)
    }

    fun deleteSession(sessionId: Long) {
        sessionRepository.findByIdOrNull(sessionId) ?: throw NotFoundException.sessionNotFound()

        sessionRepository.deleteById(sessionId)
    }

    @Transactional(readOnly = true)
    fun getSessionHome(email: String): FindSessionHomeResponse {
        val loggedInMember = memberRepository.findByEmail(email)
                ?: throw NotFoundException.memberNotFound()
        val generationMember =
                generationMemberRepository.findByGenerationAndMemberId(CURRENT_ONGOING_GENERATION, loggedInMember.id)
                        ?: return FindSessionHomeResponse.of()

        val upcomingSession = sessionRepository.findUpcomingSession(CURRENT_ONGOING_GENERATION, LocalDate.now())
                ?: return FindSessionHomeResponse.of()

        val attendance = attendanceRepository.findBySessionIdAndGenerationMemberId(upcomingSession.id, generationMember.id)
                ?: return FindSessionHomeResponse.of()

        return FindSessionHomeResponse.of(upcomingSession, attendance)
    }

}
