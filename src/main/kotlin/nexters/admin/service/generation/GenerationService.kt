package nexters.admin.service.generation

import nexters.admin.domain.generation.Generation
import nexters.admin.exception.BadRequestException
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.GenerationRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class GenerationService(
        private val generationRepository: GenerationRepository
) {

    fun createGeneration(request: CreateGenerationRequest) {
        val exist = generationRepository.findByIdOrNull(request.generation)

        if (exist != null) {
            throw BadRequestException.generationAlreadyExist()
        }

        generationRepository.save(
                Generation(
                        generation = request.generation,
                        ceo = request.ceo
                )
        )
    }

    fun deleteGeneration(generation: Long) {
        generationRepository.deleteById(generation)
    }

    fun updateGeneration(generation: Long, request: UpdateGenerationRequest) {
        val found = generationRepository.findByIdOrNull(generation) ?: throw NotFoundException.generationNotFound()

        found.apply {
            ceo = request.ceo
            status = request.status
        }

        generationRepository.save(found)
    }

    fun findGeneration(generation: Long): Generation {
        return generationRepository.findByIdOrNull(generation) ?: throw NotFoundException.generationNotFound()
    }

    fun findAllGeneration(): List<Generation> {
        return generationRepository.findAll()
    }

    fun findCurrentGeneration(): Generation {
        return generationRepository.findFirstByOrderByGenerationDesc() ?: throw NotFoundException.generationNotFound()
    }
}
