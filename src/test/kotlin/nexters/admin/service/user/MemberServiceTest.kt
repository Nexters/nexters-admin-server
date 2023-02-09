package nexters.admin.service.user

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nexters.admin.createNewGenerationMember
import nexters.admin.createNewMember
import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.user.Password
import nexters.admin.domain.user.member.Member
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.MemberRepository

class MemberServiceTest : BehaviorSpec({
    val memberRepository = mockk<MemberRepository>()
    val generationMemberRepository = mockk<GenerationMemberRepository>()

    val memberService = MemberService(memberRepository, generationMemberRepository)

    Given("회원 전체가 있는 경우") {
        val member1: Member = createNewMember()
        val member2: Member = createNewMember(name = "김태현", email = "kth990303@naver.com")
        val generationMember1: GenerationMember = createNewGenerationMember(memberId = member1.id)
        val generationMember2: GenerationMember = createNewGenerationMember(memberId = member2.id)

        every { memberRepository.findAll() } returns listOf(member1, member2)
        every { generationMemberRepository.findAllByMemberId(member1.id) } returns listOf(generationMember1)
        every { generationMemberRepository.findAllByMemberId(member2.id) } returns listOf(generationMember2)

        When("관리자가 회원 관리 페이지에 접속하면") {
            val actual = memberService.findAllByAdministrator()

            Then("회원 전체를 조회할 수 있다") {
                actual.data shouldHaveSize 2
            }
        }
    }

    Given("특정 회원이 있는 경우") {
        val member: Member = createNewMember()

        every { memberRepository.findByEmail(member.email) } returns member

        When("해당 회원이 비밀번호를 수정하면") {
            memberService.updatePassword(member, Password("2345"))

            Then("비밀번호를 수정할 수 있다") {
                member.password shouldBe Password("2345")
            }
        }

        When("해당 회원의 이메일로 회원을 조회하면") {
            val findMember = memberService.getByEmail(member.email)

            Then("해당 회원을 조회할 수 있다") {
                findMember.email shouldBe member.email
            }
        }
    }
})
