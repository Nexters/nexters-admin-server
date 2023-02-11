package nexters.admin.support.auth

import nexters.admin.exception.UnauthenticatedException
import nexters.admin.service.user.AdminService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AdminAuthInterceptor(
        private val jwtTokenProvider: JwtTokenProvider,
        private val adminService: AdminService,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest,
                           response: HttpServletResponse,
                           handler: Any): Boolean {
        val token = extractBearerToken(request)
        val username = jwtTokenProvider.getPayload(token)
        if (adminService.checkByUsername(username)) {
            return true
        }
        response.status = HttpStatus.FORBIDDEN.value()
        return false
    }

    private fun extractBearerToken(request: HttpServletRequest): String {
        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)
                ?: throw UnauthenticatedException.loginNeeded()
        return parseAuthorizationHeader(authorization)
    }

    private fun parseAuthorizationHeader(authorization: String): String {
        try {
            val authValues = authorization.split(" ")
            if (authValues[0] != "Bearer") {
                throw UnauthenticatedException.loginNeeded()
            }
            return authValues[1]
        } catch (e: IndexOutOfBoundsException) {
            throw UnauthenticatedException.loginNeeded()
        }
    }
}
