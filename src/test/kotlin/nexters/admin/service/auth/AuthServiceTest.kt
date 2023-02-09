package nexters.admin.service.auth

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import nexters.admin.createNewMember
import nexters.admin.createNewTestJwtTokenProvider
import nexters.admin.domain.user.Password
import nexters.admin.domain.user.member.Member
import nexters.admin.exception.UnauthenticatedException
import nexters.admin.repository.MemberRepository

class AuthServiceTest : BehaviorSpec({
    val memberRepository = mockk<MemberRepository>()
    val tokenProvider = createNewTestJwtTokenProvider()

    val authService = AuthService(memberRepository, tokenProvider)

    Given("가입된 회원이 있는 경우") {
        val member: Member = createNewMember()
        val invalidEmail = "invalid@email.com"

        every { memberRepository.findByEmail(member.email) } returns member
        every { memberRepository.findByEmail(invalidEmail) } returns null

        When("해당 회원이 올바르게 로그인을 하면") {
            Then("토큰이 발행될 수 있다") {
                shouldNotThrow<UnauthenticatedException> {
                    authService.generateMemberToken(LoginRequest(member.email, member.password))
                }
            }
        }

        When("해당 회원이 잘못된 비밀번호로 로그인을 하면") {
            Then("예외가 발생한다") {
                shouldThrow<UnauthenticatedException> {
                    authService.generateMemberToken(LoginRequest(member.email, Password("invalid")))
                }
            }
        }

        When("해당 회원이 잘못된 비밀번호로 로그인을 하면") {
            Then("예외가 발생한다") {
                shouldThrow<UnauthenticatedException> {
                    authService.generateMemberToken(LoginRequest(invalidEmail, Password("invalid")))
                }
            }
        }
    }
})
