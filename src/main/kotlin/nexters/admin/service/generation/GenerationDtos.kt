package nexters.admin.service.generation

import nexters.admin.domain.generation.Generation
import nexters.admin.domain.generation.GenerationStatus

data class GenerationResponses(
        val data: List<GenerationResponse?>,
)

data class GenerationResponse(
        val generation: Int,
        val status: GenerationStatus,
) {
    companion object {
        fun from(generation: Generation): GenerationResponse {
            return GenerationResponse(
                    generation = generation.generation,
                    status = generation.status
            )
        }
    }
}

data class FindCurrentGeneration(
        val generation: Int,
        val status: GenerationStatus,
)
