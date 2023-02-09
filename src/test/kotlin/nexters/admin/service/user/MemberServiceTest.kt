package nexters.admin.service.user

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import nexters.admin.createNewGenerationMember
import nexters.admin.createNewMember
import nexters.admin.domain.generation_member.GenerationMember
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
        val generationMember: GenerationMember = createNewGenerationMember()

        every { memberRepository.findAll() } returns listOf(member1, member2)
        every { generationMemberRepository.findAllByMemberId(member1.id) } returns listOf(generationMember)
        every { generationMemberRepository.findAllByMemberId(member2.id) } returns listOf(generationMember)

        When("관리자가 회원 관리 페이지에 접속하면") {
            val actual = memberService.findAllByAdministrator()

            Then("회원 전체를 조회할 수 있다") {
                actual.data shouldHaveSize 2
            }
        }
    }
})
