package nexters.admin.service.generation

import nexters.admin.domain.generation.Generation
import nexters.admin.exception.BadRequestException
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.GenerationRepository
import org.springframework.stereotype.Service

@Service
class GenerationService(
        private val generationRepository: GenerationRepository
) {
    fun createGeneration(request: CreateGenerationRequest) {
        val existingGenerationFound = generationRepository.findByGeneration(request.generation)
        if (existingGenerationFound != null) {
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
        generationRepository.deleteByGeneration(generation)
    }

    fun updateGeneration(generation: Long, request: UpdateGenerationRequest) {
        val found = generationRepository.findByGeneration(generation) ?: throw NotFoundException.generationNotFound()
        found.apply {
            ceo = request.ceo
            status = request.status
        }
        generationRepository.save(found)
    }

    fun findGeneration(generation: Long): Generation {
        return generationRepository.findByGeneration(generation) ?: throw NotFoundException.generationNotFound()
    }

    fun findAllGeneration(): List<Generation> {
        return generationRepository.findAll()
    }

    // TODO: MAX(generation) 사용하도록 수정
    fun findCurrentGeneration(): Generation {
        return generationRepository.findFirstByOrderByGenerationDesc() ?: throw NotFoundException.generationNotFound()
    }
}
