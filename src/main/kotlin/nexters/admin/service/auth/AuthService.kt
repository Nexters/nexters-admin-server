package nexters.admin.service.auth

import nexters.admin.support.auth.JwtTokenProvider
import nexters.admin.exception.UnauthenticatedException
import nexters.admin.repository.AdministratorRepository
import nexters.admin.repository.MemberRepository
import org.springframework.stereotype.Service

@Service
class AuthService(
        private val adminRepository: AdministratorRepository,
        private val memberRepository: MemberRepository,
        private val jwtTokenProvider: JwtTokenProvider
) {
    fun generateAdminToken(request: AdminLoginRequest): String {
        val admin = adminRepository.findByUsername(request.username)
                ?: throw UnauthenticatedException.loginFail()
        if (!admin.isSamePassword(request.password)) {
            throw UnauthenticatedException.loginFail()
        }
        admin.updateLastAccessTime()
        return jwtTokenProvider.generateToken(request.username)
    }

    fun generateMemberToken(request: LoginRequest): String {
        val member = memberRepository.findByEmail(request.email)
                ?: throw UnauthenticatedException.loginFail()
        if (!member.isSamePassword(request.password)) {
            throw UnauthenticatedException.loginFail()
        }
        return jwtTokenProvider.generateToken(member.email)
    }
}
