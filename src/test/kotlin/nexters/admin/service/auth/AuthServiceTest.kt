package nexters.admin.service.auth

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import nexters.admin.createNewMember
import nexters.admin.domain.user.Password
import nexters.admin.domain.user.member.Member
import nexters.admin.exception.UnauthenticatedException
import nexters.admin.repository.MemberRepository
import nexters.admin.support.auth.JwtTokenProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AuthServiceTest(
        @Autowired private val memberRepository: MemberRepository,
        @Autowired private val tokenProvider: JwtTokenProvider,
) {
    val authService = AuthService(memberRepository, tokenProvider)

    @AfterEach
    fun tearDown() {
        memberRepository.deleteAll()
    }

    @Test
    fun `로그인 시 토큰 발행`() {
        val member: Member = createNewMember()

        memberRepository.save(member)

        shouldNotThrow<UnauthenticatedException> {
            authService.generateMemberToken(LoginRequest(member.email, member.password))
        }
    }

    @Test
    fun `로그인 시 잘못된 비밀번호로 입력할 경우 예외 발생`() {
        val member: Member = createNewMember()

        memberRepository.save(member)

        shouldThrow<UnauthenticatedException> {
            authService.generateMemberToken(LoginRequest(member.email, Password("invalid")))
        }
    }

    @Test
    fun `미회원이 로그인할 경우 예외 발생`() {
        val member: Member = createNewMember()

        memberRepository.save(member)

        shouldThrow<UnauthenticatedException> {
            authService.generateMemberToken(LoginRequest("invalid@email.com", member.password))
        }
    }
}
