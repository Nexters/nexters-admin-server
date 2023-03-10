package nexters.admin.repository

import nexters.admin.domain.generation.Generation
import org.springframework.data.jpa.repository.JpaRepository

interface GenerationRepository : JpaRepository<Generation, Long> {
    fun findFirstByOrderByGenerationDesc(): Generation?
    fun deleteByGeneration(generation: Int)
    fun findByGeneration(generation: Int): Generation?
}
