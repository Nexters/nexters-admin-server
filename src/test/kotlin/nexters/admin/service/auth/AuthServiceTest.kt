package nexters.admin.service.auth

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import nexters.admin.testsupport.createNewAdmin
import nexters.admin.testsupport.createNewMember
import nexters.admin.domain.user.administrator.Administrator
import nexters.admin.domain.user.member.Member
import nexters.admin.exception.UnauthenticatedException
import nexters.admin.repository.AdministratorRepository
import nexters.admin.repository.MemberRepository
import nexters.admin.support.auth.JwtTokenProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AuthServiceTest(
        @Autowired private val administratorRepository: AdministratorRepository,
        @Autowired private val memberRepository: MemberRepository,
        @Autowired private val tokenProvider: JwtTokenProvider,
) {
    val authService = AuthService(administratorRepository, memberRepository, tokenProvider)

    @AfterEach
    fun tearDown() {
        memberRepository.deleteAll()
        administratorRepository.deleteAll()
    }

    @Test
    fun `일반 회원 로그인 시 토큰 발행`() {
        val member: Member = createNewMember(password = "1234")

        memberRepository.save(member)

        shouldNotThrow<UnauthenticatedException> {
            authService.generateMemberToken(MemberLoginRequest(member.email, "1234"))
        }
    }

    @Test
    fun `관리자 회원 로그인 시 토큰 발행`() {
        val administrator: Administrator = createNewAdmin(password = "1234")

        administratorRepository.save(administrator)

        shouldNotThrow<UnauthenticatedException> {
            authService.generateAdminToken(AdminLoginRequest(administrator.username, "1234"))
        }
    }

    @Test
    fun `로그인 시 잘못된 비밀번호로 입력할 경우 예외 발생`() {
        val member: Member = createNewMember()

        memberRepository.save(member)

        shouldThrow<UnauthenticatedException> {
            authService.generateMemberToken(MemberLoginRequest(member.email, "invalid"))
        }
    }

    @Test
    fun `미회원이 로그인할 경우 예외 발생`() {
        val member: Member = createNewMember()

        memberRepository.save(member)

        shouldThrow<UnauthenticatedException> {
            authService.generateMemberToken(MemberLoginRequest("invalid@email.com", member.password.value))
        }
    }
}
