package nexters.admin.repository

import nexters.admin.domain.session.Session
import org.springframework.data.jpa.repository.JpaRepository

interface SessionRepository: JpaRepository<Session, Long> {
    fun findAllByGeneration(generation: Int): List<Session>
}
