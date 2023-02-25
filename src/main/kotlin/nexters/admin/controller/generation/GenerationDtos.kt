package nexters.admin.controller.generation

data class CreateGenerationRequest(
        val generation: Int,
)

data class UpdateGenerationRequest(
        val status: String,
)
