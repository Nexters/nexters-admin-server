package nexters.admin.service.user

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nexters.admin.controller.user.UpdateMemberRequest
import nexters.admin.createNewGenerationMember
import nexters.admin.createNewMember
import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.user.Password
import nexters.admin.domain.user.member.Member
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.MemberRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest
class MemberServiceTest(
        @Autowired private val memberRepository: MemberRepository,
        @Autowired private val generationMemberRepository: GenerationMemberRepository,
) : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    val memberService = MemberService(memberRepository, generationMemberRepository)

    Given("회원 전체가 있는 경우") {
        val member1: Member = memberRepository.save(createNewMember())
        val member2: Member = memberRepository.save(createNewMember(name = "김태현", email = "kth990303@naver.com"))
        val generationMember1: GenerationMember = createNewGenerationMember(memberId = member1.id)
        val generationMember2: GenerationMember = createNewGenerationMember(memberId = member2.id)

        generationMemberRepository.save(generationMember1)
        generationMemberRepository.save(generationMember2)

        When("관리자가 회원 관리 페이지에 접속하면") {
            val actual = memberService.findAllByAdministrator()

            Then("회원 전체를 조회할 수 있다") {
                actual.data shouldHaveSize 2
            }
        }
    }

    Given("특정 회원이 있는 경우") {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)

        generationMemberRepository.save(generationMember)

        When("관리자가 해당 회원에 대한 정보를 수정하면") {
            memberService.updateMemberByAdministrator(
                    member.id,
                    UpdateMemberRequest(
                            name = "김태현",
                            gender = "남자",
                            email = member.email,
                            phoneNumber = member.phoneNumber,
                            generations = listOf(21)
                    )
            )

            Then("회원 정보가 수정된다") {
                val findMember = memberRepository.findByEmail(member.email)
                        ?: throw NotFoundException.memberNotFound()
                findMember.name shouldBe "김태현"
            }

            Then("수정 요청된 기수에 대한 정보가 추가된다") {
                val generations = generationMemberRepository.findAllByMemberId(member.id)

                generations shouldHaveSize 1
                generations[0].generation shouldBe 21
            }

            Then("수정 요청된 기수 외의 정보는 삭제된다") {
                val generations = generationMemberRepository.findAllByMemberId(member.id)

                generations shouldHaveSize 1
                generations[0].generation shouldNotBe 22
            }
        }

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
