package nexters.admin.service.auth

import javax.validation.constraints.Email

data class AdminLoginRequest(
        val username: String,
        val password: String,
)

data class MemberLoginRequest(
        @field:Email
        val email: String,
        val password: String,
)
