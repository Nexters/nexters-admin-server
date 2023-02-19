package nexters.admin.service.session

import nexters.admin.domain.session.Session
import nexters.admin.exception.BadRequestException
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.SessionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class SessionService(
        private val sessionRepository: SessionRepository
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
        val session = sessionRepository.findByIdOrNull(sessionId) ?: throw NotFoundException.sessionNotFound()

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

}
