package nexters.admin.service.auth

import nexters.admin.domain.user.Password
import javax.validation.constraints.Email

data class LoginRequest(
        @field:Email
        val email: String,
        val password: Password
)
