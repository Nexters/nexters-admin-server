package nexters.admin.service.auth

import nexters.admin.support.auth.JwtTokenProvider
import nexters.admin.support.exception.UnauthenticatedException
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
        member.checkSamePassword(request.password)
        return jwtTokenProvider.generateToken(member.email)
    }
}
