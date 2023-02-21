package nexters.admin.service.session

import nexters.admin.domain.session.Session
import nexters.admin.domain.user.member.Member
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.AttendanceRepository
import nexters.admin.repository.GenerationMemberRepository
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
        private val sessionRepository: SessionRepository,
        private val generationMemberRepository: GenerationMemberRepository,
        private val attendanceRepository: AttendanceRepository
) {

    fun findSessionByGeneration(generation: Int): List<Session> {
        return sessionRepository.findAllByGeneration(generation)
    }

    fun createSession(request: CreateSessionRequest): Long {
        val savedSession = sessionRepository.save(
                Session(
                        title = request.title,
                        description = request.description,
                        message = request.message,
                        generation = request.generation,
                        sessionTime = request.sessionTime,
                        week = request.week,
                        startAttendTime = request.startAttendTime,
                        endAttendTime = request.endAttendTime
                )
        )

        return savedSession.id
    }

    fun findSession(id: Long): Session? {
        return sessionRepository.findByIdOrNull(id)
    }

    fun updateSession(sessionId: Long, request: UpdateSessionRequest) {
        val session = sessionRepository.findByIdOrNull(sessionId)
                ?: throw NotFoundException.sessionNotFound()

        session.apply {
            title = request.title
            description = request.description
            message = request.message
            generation = request.generation
            sessionTime = request.sessionTime
            week = request.week
            startAttendTime = request.startAttendTime
            endAttendTime = request.endAttendTime
        }

        sessionRepository.save(session)
    }

    fun deleteSession(sessionId: Long) {
        sessionRepository.findByIdOrNull(sessionId) ?: throw NotFoundException.sessionNotFound()

        sessionRepository.deleteById(sessionId)
    }

    @Transactional(readOnly = true)
    fun getSessionHome(loggedInMember: Member): FindSessionHomeResponse {
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
