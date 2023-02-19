package nexters.admin.support.auth

import nexters.admin.exception.UnauthenticatedException
import org.springframework.http.HttpHeaders
import org.springframework.web.context.request.NativeWebRequest
import javax.servlet.http.HttpServletRequest

class AuthorizationHeaderUtils {
    companion object {
        fun extractBearerToken(request: NativeWebRequest): String {
            val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)
                    ?: throw UnauthenticatedException.loginNeeded()
            return parseAuthorizationHeader(authorization)
        }

        fun extractBearerToken(request: HttpServletRequest): String {
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
}
