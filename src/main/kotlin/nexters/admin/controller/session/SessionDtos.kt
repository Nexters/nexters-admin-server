package nexters.admin.controller.session

import java.time.LocalDate
import java.time.LocalDateTime

data class CreateSessionRequest(
        val title: String,
        val description: String?,
        val message: String?,
        val generation: Int,
        val sessionTime: LocalDate,
        val week: Int,
        val startAttendTime: LocalDateTime,
        val endAttendTime: LocalDateTime
)

data class UpdateSessionRequest(
        val title: String,
        val description: String?,
        val message: String?,
        val generation: Int,
        val sessionTime: LocalDate,
        val week: Int,
        val startAttendTime: LocalDateTime,
        val endAttendTime: LocalDateTime
)
