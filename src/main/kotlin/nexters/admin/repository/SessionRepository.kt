package nexters.admin.repository

import nexters.admin.domain.session.Session
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

fun SessionRepository.findUpcomingSession(generation: Int, today: LocalDate): Session? {
    return findTopByGenerationAndSessionDateGreaterThanEqualOrderBySessionDateAsc(generation, today)
}

interface SessionRepository : JpaRepository<Session, Long> {
    fun findAllByGeneration(generation: Int): List<Session>
    fun findTopByGenerationAndSessionDateGreaterThanEqualOrderBySessionDateAsc(generation: Int, today: LocalDate): Session?
}
