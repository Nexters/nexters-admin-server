package nexters.admin.controller.auth

data class TokenResponse(val token: String)

data class LoggedInMemberRequest(val email: String)
