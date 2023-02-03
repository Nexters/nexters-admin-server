package nexters.admin.support.auth

import nexters.admin.support.exception.UnauthenticatedException
import nexters.admin.domain.user.member.Member
import nexters.admin.service.user.MemberService
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class LoginUserResolver(
        private val jwtTokenProvider: JwtTokenProvider,
        private val memberService: MemberService
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(LoggedInMember::class.java)
    }

    override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?
    ): Member {
        val token = extractBearerToken(webRequest)
        val userEmail = jwtTokenProvider.getPayload(token)
        return memberService.getByEmail(userEmail)
    }

    private fun extractBearerToken(request: NativeWebRequest): String {
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
