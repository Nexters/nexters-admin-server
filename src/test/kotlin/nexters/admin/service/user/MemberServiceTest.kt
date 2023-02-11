package nexters.admin.service.user

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nexters.admin.PHONE_NUMBER
import nexters.admin.controller.user.CreateMemberRequest
import nexters.admin.controller.user.UpdateMemberRequest
import nexters.admin.createNewGenerationMember
import nexters.admin.createNewMember
import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.generation_member.Position
import nexters.admin.domain.generation_member.SubPosition
import nexters.admin.domain.user.Password
import nexters.admin.domain.user.member.Member
import nexters.admin.domain.user.member.MemberStatus
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.MemberRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest
class MemberServiceTest(
        @Autowired private val memberRepository: MemberRepository,
        @Autowired private val generationMemberRepository: GenerationMemberRepository,
) {
    val memberService = MemberService(memberRepository, generationMemberRepository)

    @AfterEach
    fun tearDown() {
        memberRepository.deleteAll()
        generationMemberRepository.deleteAll()
    }

    @Test
    fun `회원 저장`() {
        memberService.createMemberByAdministrator(
                CreateMemberRequest(
                        "김태현",
                        "남자",
                        "kth990303@naver.com",
                        PHONE_NUMBER,
                        mutableListOf(22),
                        "개발자",
                        "백엔드",
                        "미이수",
                        false
                )
        )

        val actual = memberRepository.findByEmail("kth990303@naver.com")

        actual shouldNotBe null
        actual?.name shouldBe "김태현"
    }

    @Test
    fun `회원 저장 시 최신 기수회원 정보 저장`() {
        val memberId = memberService.createMemberByAdministrator(
                CreateMemberRequest(
                        "김태현",
                        "남자",
                        "kth990303@naver.com",
                        PHONE_NUMBER,
                        mutableListOf(14, 19, 22),
                        "개발자",
                        "백엔드",
                        "미이수",
                        false
                )
        )

        val currentGenerationMember = generationMemberRepository.findByGenerationAndMemberId(22, memberId)

        currentGenerationMember shouldNotBe null
        currentGenerationMember?.position shouldBe Position.DEVELOPER
        currentGenerationMember?.subPosition shouldBe SubPosition.BE
        currentGenerationMember?.score shouldBe 100
    }

    @Test
    fun `회원 저장 시 이전 기수회원 정보의 직군은 최신직군으로, 점수는 null 로 저장`() {
        val memberId = memberService.createMemberByAdministrator(
                CreateMemberRequest(
                        "김태현",
                        "남자",
                        "kth990303@naver.com",
                        PHONE_NUMBER,
                        mutableListOf(14, 19, 22),
                        "개발자",
                        "백엔드",
                        "미이수",
                        false
                )
        )

        val currentGenerationMember = generationMemberRepository.findByGenerationAndMemberId(14, memberId)

        currentGenerationMember shouldNotBe null
        currentGenerationMember?.position shouldBe Position.DEVELOPER
        currentGenerationMember?.subPosition shouldBe SubPosition.BE
        currentGenerationMember?.score shouldBe null
    }

    @Test
    fun `회원 저장 시 회원이 활동하지 않은 기수는 저장되지 않는지 확인`() {
        val memberId = memberService.createMemberByAdministrator(
                CreateMemberRequest(
                        "김태현",
                        "남자",
                        "kth990303@naver.com",
                        PHONE_NUMBER,
                        mutableListOf(14, 19, 22),
                        "개발자",
                        "백엔드",
                        "미이수",
                        false
                )
        )

        val currentGenerationMember = generationMemberRepository.findByGenerationAndMemberId(999, memberId)

        currentGenerationMember shouldBe null
    }

    @Test
    fun `회원 전체 조회`() {
        val member1: Member = memberRepository.save(createNewMember())
        val member2: Member = memberRepository.save(createNewMember(name = "김태현", email = "kth990303@naver.com"))
        val generationMember1: GenerationMember = createNewGenerationMember(memberId = member1.id, generation = 22)
        val generationMember2: GenerationMember = createNewGenerationMember(memberId = member2.id, generation = 15)
        generationMemberRepository.save(generationMember1)
        generationMemberRepository.save(generationMember2)

        val actual = memberService.findAllByAdministrator()

        actual.data shouldHaveSize 2
    }

    @Test
    fun `회원 정보 수정`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)
        generationMemberRepository.save(generationMember)

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

        val findMember = memberRepository.findByEmail(member.email)
                ?: throw NotFoundException.memberNotFound()
        findMember.name shouldBe "김태현"
    }

    @Test
    fun `회원 정보 수정 시 기수 추가가 올바르게 되는지 확인`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)
        generationMemberRepository.save(generationMember)

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

        val generations = generationMemberRepository.findAllByMemberId(member.id)

        generations shouldHaveSize 1
        generations[0].generation shouldBe 21
    }

    @Test
    fun `회원 정보 수정 시 기수 삭제가 올바르게 되는지 확인`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)
        generationMemberRepository.save(generationMember)

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

        val generations = generationMemberRepository.findAllByMemberId(member.id)

        generations shouldHaveSize 1
        generations[0].generation shouldNotBe 22
    }

    @Test
    fun `회원 활동구분 수정`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)
        generationMemberRepository.save(generationMember)

        memberService.updateStatusByAdministrator(member.id, "수료")

        val findMember = memberRepository.findByEmail(member.email)
                ?: throw NotFoundException.memberNotFound()

        findMember.status shouldBe MemberStatus.CERTIFICATED
    }

    @Test
    fun `회원 직군 수정`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)
        generationMemberRepository.save(generationMember)

        memberService.updatePositionByAdministrator(member.id, "디자이너", null)

        val generations = generationMemberRepository.findAllByMemberId(member.id)
        generations[0].position shouldBe Position.DESIGNER
    }

    @Test
    fun `회원 비밀번호 수정`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)
        generationMemberRepository.save(generationMember)

        memberService.updatePassword(member, "2345")

        member.password shouldBe Password("2345")
    }

    @Test
    fun `회원 이메일로 해당 회원 조회`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)
        generationMemberRepository.save(generationMember)

        val findMember = memberService.getByEmail(member.email)

        findMember.email shouldBe member.email
    }

    @Test
    fun `회원 삭제`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)
        generationMemberRepository.save(generationMember)

        memberService.deleteByAdministrator(member.id)

        memberRepository.findByIdOrNull(member.id) shouldBe null
    }

    @Test
    fun `회원 삭제 시 관련 기수회원 정보 삭제 확인`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)
        generationMemberRepository.save(generationMember)

        memberService.deleteByAdministrator(member.id)

        val generations = generationMemberRepository.findAllByMemberId(member.id)
        generations shouldHaveSize 0
    }
}
