package nexters.admin.service.auth

import nexters.admin.support.auth.JwtTokenProvider
import nexters.admin.exception.UnauthenticatedException
import nexters.admin.repository.MemberRepository
import org.springframework.stereotype.Service

@Service
class AuthService(
        private val memberRepository: MemberRepository,
        private val jwtTokenProvider: JwtTokenProvider
) {
    fun generateMemberToken(request: LoginRequest): String {
        val member = memberRepository.findByEmail(request.email)
                ?: throw UnauthenticatedException.loginFail()
        if (!member.isSamePassword(request.password)) {
            throw UnauthenticatedException.loginFail()
        }
        return jwtTokenProvider.generateToken(member.email)
    }
}
