package nexters.admin.support.auth

import nexters.admin.service.user.AdminService
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
        val token = AuthorizationHeaderUtils.extractBearerToken(request)
        val username = jwtTokenProvider.getPayload(token)
        if (adminService.checkByUsername(username)) {
            return true
        }
        response.status = HttpStatus.FORBIDDEN.value()
        return false
    }
}
