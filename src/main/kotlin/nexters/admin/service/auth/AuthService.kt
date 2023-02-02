package nexters.admin.service.auth

import nexters.admin.common.exception.UnauthenticatedException
import nexters.admin.repository.MemberRepository
import org.springframework.stereotype.Service

@Service
class AuthService(
        val memberRepository: MemberRepository,
        val jwtTokenProvider: JwtTokenProvider
) {
    fun generateMemberToken(request: LoginRequest): String {
        val member = memberRepository.findByEmail(request.email)
                ?: throw UnauthenticatedException.loginFail()
        member.validatePassword(request.password)
        return jwtTokenProvider.generateToken(member.email)
    }
}
