package nexters.admin.service.generation

import nexters.admin.controller.generation.CreateGenerationRequest
import nexters.admin.controller.generation.UpdateGenerationRequest
import nexters.admin.domain.generation.Generation
import nexters.admin.domain.generation.GenerationStatus
import nexters.admin.exception.BadRequestException
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.GenerationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GenerationService(
        private val generationRepository: GenerationRepository,
        private val generationMemberRepository: GenerationMemberRepository,
) {
    fun createGeneration(request: CreateGenerationRequest) {
        val existingGenerationFound = generationRepository.findByGeneration(request.generation)
        if (existingGenerationFound != null) {
            throw BadRequestException.generationAlreadyExist()
        }
        generationRepository.save(
                Generation(
                        generation = request.generation,
                )
        )
    }

    @Transactional(readOnly = true)
    fun findGeneration(generation: Int): GenerationResponse {
        return generationRepository.findByGeneration(generation)
                ?.let { GenerationResponse(it.generation, it.status) }
                ?: throw NotFoundException.generationNotFound()
    }

    @Transactional(readOnly = true)
    fun findAllGeneration(): GenerationResponses {
        return GenerationResponses(
                generationRepository.findAll()
                        .sortedByDescending { it.generation }
                        .map { GenerationResponse(it.generation, it.status) }
        )
    }

    // TODO: MAX(generation) 사용하도록 수정
    @Transactional(readOnly = true)
    fun findCurrentGeneration(): FindCurrentGeneration {
        return generationRepository.findFirstByOrderByGenerationDesc()
                ?.let { FindCurrentGeneration(it.generation, it.status) }
                ?: throw NotFoundException.generationNotFound()
    }

    fun updateGeneration(generation: Int, request: UpdateGenerationRequest) {
        val found = generationRepository.findByGeneration(generation) ?: throw NotFoundException.generationNotFound()
        found.apply { status = GenerationStatus.from(request.status) }
        generationRepository.save(found)
    }

    fun deleteGeneration(generation: Int) {
        val generationMembers = generationMemberRepository.findByGeneration(generation)
        if (generationMembers.isNotEmpty()) {
            throw BadRequestException.existsGenerationMembers()
        }

        generationRepository.deleteByGeneration(generation)
    }
}
