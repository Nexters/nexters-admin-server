package nexters.admin.controller.session

import java.time.LocalDate

data class CreateSessionRequest(
        val title: String,
        val description: String?,
        val generation: Int,
        val sessionDate: LocalDate,
        val week: Int,
)

data class UpdateSessionRequest(
        val title: String,
        val description: String?,
        val generation: Int,
        val sessionDate: LocalDate,
        val week: Int,
)
