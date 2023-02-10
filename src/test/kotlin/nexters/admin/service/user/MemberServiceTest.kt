package nexters.admin.service.user

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import nexters.admin.createNewGenerationMember
import nexters.admin.createNewMember
import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.generation_member.Position
import nexters.admin.domain.user.member.Member
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.MemberRepository
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest
class MemberServiceTest : BehaviorSpec({
    val memberRepository = mockk<MemberRepository>()
    val generationMemberRepository = mockk<GenerationMemberRepository>()

    val memberService = MemberService(memberRepository, generationMemberRepository)

    Given("특정 회원이 있는 경우") {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember1: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 22, position = Position.DEVELOPER)
        val generationMember2: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 23, position = Position.DESIGNER)
        generationMemberRepository.save(generationMember2)
        generationMemberRepository.save(generationMember1)

        When("본인의 프로필을 조회하면") {
            val findProfile = memberService.getProfile(member)

            Then("본인의 정보를 조회할 수 있다") {
                findProfile.name shouldBe member.name
            }
            Then("가장 최근 기수의 정보를 조회할 수 있다.") {
                findProfile.generation shouldBe generationMember2.generation
            }
        }
    }
    Given("기수 정보가 없는 회원의 경우") {
        val member: Member = memberRepository.save(createNewMember())

        When("본인의 프로필을 조회하면") {
            val findProfile = memberService.getProfile(member)

            Then("본인의 개인 정보를 조회할 수 있다.") {
                findProfile.name shouldBe member.name
            }
            Then("기수 정보는 0기로 표시된다.") {
                findProfile.generation shouldBe 0
            }
        }
    }
})
