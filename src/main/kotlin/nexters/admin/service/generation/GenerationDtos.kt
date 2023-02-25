package nexters.admin.service.generation

import nexters.admin.domain.generation.Generation
import nexters.admin.domain.generation.GenerationStatus

data class CreateGenerationRequest(
        val generation: Int,
        val ceo: String,
)

data class UpdateGenerationRequest(
        val ceo: String,
        val status: GenerationStatus
)

data class GenerationResponse(
        val generation: Int,
        val ceo: String?,
        val status: GenerationStatus,
) {
    companion object {
        fun from(generation: Generation): GenerationResponse {
            return GenerationResponse(
                    generation = generation.generation,
                    ceo = generation.ceo,
                    status = generation.status
            )
        }
    }
}
