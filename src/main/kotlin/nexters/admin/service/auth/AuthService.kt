package nexters.admin.service.auth

import nexters.admin.domain.user.Password
import nexters.admin.exception.UnauthenticatedException
import nexters.admin.repository.AdministratorRepository
import nexters.admin.repository.MemberRepository
import nexters.admin.support.auth.JwtTokenProvider
import org.springframework.stereotype.Service

@Service
class AuthService(
        private val adminRepository: AdministratorRepository,
        private val memberRepository: MemberRepository,
        private val jwtTokenProvider: JwtTokenProvider,
) {
    fun generateAdminToken(request: AdminLoginRequest): String {
        val admin = adminRepository.findByUsername(request.username)
                ?: throw UnauthenticatedException.loginFail()
        if (!admin.isSamePassword(Password(request.password))) {
            throw UnauthenticatedException.loginFail()
        }
        admin.updateLastAccessTime()
        return jwtTokenProvider.generateToken(request.username)
    }

    fun generateMemberToken(request: MemberLoginRequest): MemberLoginResponse {
        val member = memberRepository.findByEmail(request.email)
                ?: throw UnauthenticatedException.loginFail()
        if (!member.isSamePassword(Password(request.password))) {
            throw UnauthenticatedException.loginFail()
        }
        val token = jwtTokenProvider.generateToken(member.email)
        return MemberLoginResponse(token, !member.hasChangedPassword)
    }
}
