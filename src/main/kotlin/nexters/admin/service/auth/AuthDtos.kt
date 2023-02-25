package nexters.admin.service.auth

import javax.validation.constraints.Email

data class AdminLoginRequest(
        val username: String,
        val password: String,
)

data class MemberLoginRequest(
        @field:Email(message = "잘못된 이메일 형식")
        val email: String,
        val password: String,
)

data class MemberLoginResponse(
        val token: String,
        val needPasswordReset: Boolean,
)
