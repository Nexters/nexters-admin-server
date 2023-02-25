package nexters.admin.service.user

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nexters.admin.controller.user.CreateMemberRequest
import nexters.admin.testsupport.createNewGenerationMember
import nexters.admin.testsupport.createNewMember
import nexters.admin.domain.generation.Generation
import nexters.admin.domain.generation.GenerationStatus
import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.generation_member.Position
import nexters.admin.domain.generation_member.SubPosition
import nexters.admin.domain.user.Password
import nexters.admin.domain.user.member.Member
import nexters.admin.domain.user.member.MemberStatus
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.GenerationRepository
import nexters.admin.repository.MemberRepository
import nexters.admin.testsupport.ApplicationTest
import nexters.admin.testsupport.PHONE_NUMBER
import nexters.admin.testsupport.createUpdateMemberRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

@ApplicationTest
class MemberServiceTest(
        @Autowired private val memberService: MemberService,
        @Autowired private val memberRepository: MemberRepository,
        @Autowired private val generationMemberRepository: GenerationMemberRepository,
        @Autowired private val generationRepository: GenerationRepository,
) {
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
                )
        )

        val currentGenerationMember = generationMemberRepository.findByGenerationAndMemberId(999, memberId)

        currentGenerationMember shouldBe null
    }

    @Test
    fun `회원 복수 생성시 엑셀 정보를 토대로 회원과 기수 회원 모두 생성`() {
        createLatestGeneration(21)

        val generation = 22L
        val excelInput = mutableMapOf(
                "name" to mutableListOf("정진우", "김민수", "최다예"),
                "gender" to mutableListOf("남자", "남자", "여자"),
                "email" to mutableListOf("jinwoo@gmail.com", "ming@gmail.com", "dayeah@gmail.com"),
                "phone_number" to mutableListOf("01012345678", "01012345679", "01012345670"),
                "position" to mutableListOf("개발자", "운영진", "디자이너"),
                "sub_position" to mutableListOf("프론트엔드", "CTO", ""),
                "status" to mutableListOf("미이수", "수료", "제명")
        )

        memberService.createGenerationMembers(generation, excelInput)
        memberRepository.flush()
        generationMemberRepository.flush()

        val actualMemberIds = memberRepository.findAll().map { it.id }
        val actualGenerationMembers = generationMemberRepository.findAllByMemberIdIn(actualMemberIds)
        actualMemberIds shouldHaveSize 3
        actualGenerationMembers shouldHaveSize 3
    }

    @Test
    fun `회원 복수 생성시 이메일을 기준으로 이미 생성된 회원 정보는 이메일을 그대로 덮어씌어짐`() {
        createLatestGeneration(21)

        val generation = 22
        val excelInput = mutableMapOf(
                "name" to mutableListOf("정진우", "김민수", "최다예"),
                "gender" to mutableListOf("남자", "남자", "여자"),
                "email" to mutableListOf("jinwoo@gmail.com", "ming@gmail.com", "dayeah@gmail.com"),
                "phone_number" to mutableListOf("01012345678", "01012345679", "01012345670"),
                "position" to mutableListOf("개발자", "운영진", "디자이너"),
                "sub_position" to mutableListOf("프론트엔드", "CTO", ""),
                "status" to mutableListOf("미이수", "수료", "제명")
        )
        val existingMember = memberRepository.save(createNewMember(email = "jinwoo@gmail.com", name = "기존이름"))
        memberRepository.save(createNewMember(email = "not@matching.email"))
        memberService.createGenerationMembers(generation.toLong(), excelInput)
        memberRepository.flush()
        generationMemberRepository.flush()

        memberRepository.findByEmail(existingMember.email)?.name shouldBe "정진우"
        memberRepository.findAll() shouldHaveSize 4
        generationMemberRepository.findAll() shouldHaveSize 3
    }

    @Test
    fun `회원 복수 생성시 이메일과 기수 정보를 기준으로 이미 생성된 기수회원의 직군 정보만 그대로 덮어씌어짐`() {
        createLatestGeneration(21)

        val generation = 22
        val excelInput = mutableMapOf(
                "name" to mutableListOf("정진우", "김민수", "최다예"),
                "gender" to mutableListOf("남자", "남자", "여자"),
                "email" to mutableListOf("jinwoo@gmail.com", "ming@gmail.com", "dayeah@gmail.com"),
                "phone_number" to mutableListOf("01012345678", "01012345679", "01012345670"),
                "position" to mutableListOf("개발자", "운영진", "디자이너"),
                "sub_position" to mutableListOf("프론트엔드", "CTO", ""),
                "status" to mutableListOf("미이수", "수료", "제명")
        )
        val existingMember = memberRepository.save(createNewMember(email = "jinwoo@gmail.com"))
        val existingMatchingGenerationMember = generationMemberRepository.save(
                createNewGenerationMember(memberId = existingMember.id, generation = generation, position = Position.NULL)
        )
        generationMemberRepository.save(createNewGenerationMember(memberId = existingMember.id, generation = 99999))
        memberService.createGenerationMembers(generation.toLong(), excelInput)
        memberRepository.flush()
        generationMemberRepository.flush()

        generationMemberRepository.findByIdOrNull(existingMatchingGenerationMember.id)?.position shouldBe Position.DEVELOPER
        memberRepository.findAll() shouldHaveSize 3
        generationMemberRepository.findAll() shouldHaveSize 4
    }

    private fun createLatestGeneration(generation: Int) {
        generationRepository.save(Generation(generation, GenerationStatus.FINISH_ACTIVITY))
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
                createUpdateMemberRequest()
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
                createUpdateMemberRequest()
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
                createUpdateMemberRequest()
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

    @Test
    fun `내 정보 조회`() {
        val member: Member = memberRepository.save(createNewMember(name = "정설희"))
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 22, position = Position.DEVELOPER)
        generationMemberRepository.save(generationMember)

        val profile = memberService.getProfile(member)

        profile.name shouldBe "정설희"
        profile.generation shouldBe 22
        profile.position shouldBe Position.DEVELOPER.value
    }

    @Test
    fun `두 개 이상의 기수 정보가 있는 회원이 내 정보 조회시 최신 기수 정보 조회`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember1: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 21, position = Position.DEVELOPER)
        val generationMember2: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 22, position = Position.DESIGNER)
        generationMemberRepository.save(generationMember1)
        generationMemberRepository.save(generationMember2)

        val profile = memberService.getProfile(member)

        profile.generation shouldBe 22
        profile.position shouldBe Position.DESIGNER.value
    }

    @Test
    fun `기수 정보가 없는 회원이 내 정보 조회시 0기로 표시`() {
        val member: Member = memberRepository.save(createNewMember())

        val profile = memberService.getProfile(member)

        profile.generation shouldBe 0
        profile.position shouldBe ""
    }
}
