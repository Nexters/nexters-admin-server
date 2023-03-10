package nexters.admin.service.user

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nexters.admin.controller.user.CreateMemberRequest
import nexters.admin.domain.attendance.AttendanceStatus.ATTENDED
import nexters.admin.domain.attendance.AttendanceStatus.PENDING
import nexters.admin.domain.generation.Generation
import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.generation_member.Position
import nexters.admin.domain.generation_member.SubPosition
import nexters.admin.domain.user.Password
import nexters.admin.domain.user.member.Member
import nexters.admin.domain.user.member.MemberStatus
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.AttendanceRepository
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.GenerationRepository
import nexters.admin.repository.MemberRepository
import nexters.admin.repository.SessionRepository
import nexters.admin.testsupport.ApplicationTest
import nexters.admin.testsupport.PHONE_NUMBER
import nexters.admin.testsupport.createExcelInput
import nexters.admin.testsupport.createNewAttendance
import nexters.admin.testsupport.createNewGenerationMember
import nexters.admin.testsupport.createNewMember
import nexters.admin.testsupport.createNewSession
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
        @Autowired private val sessionRepository: SessionRepository,
        @Autowired private val attendanceRepository: AttendanceRepository,
) {
    @Test
    fun `?????? ??????`() {
        memberService.createMemberByAdministrator(
                CreateMemberRequest(
                        "?????????",
                        "??????",
                        "kth990303@naver.com",
                        PHONE_NUMBER,
                        mutableListOf(22),
                        "?????????",
                        "?????????",
                        "?????????",
                )
        )

        val actual = memberRepository.findByEmail("kth990303@naver.com")

        actual shouldNotBe null
        actual?.name shouldBe "?????????"
    }

    @Test
    fun `?????? ?????? ??? ?????? ???????????? ?????? ??????`() {
        generationRepository.save(Generation(21))
        generationRepository.save(Generation(22))
        val memberId = memberService.createMemberByAdministrator(
                CreateMemberRequest(
                        "?????????",
                        "??????",
                        "kth990303@naver.com",
                        PHONE_NUMBER,
                        mutableListOf(14, 19, 22),
                        "?????????",
                        "?????????",
                        "?????????",
                )
        )

        val currentGenerationMember = generationMemberRepository.findByGenerationAndMemberId(22, memberId)

        currentGenerationMember shouldNotBe null
        currentGenerationMember?.position shouldBe Position.DEVELOPER
        currentGenerationMember?.subPosition shouldBe SubPosition.BE
        currentGenerationMember?.score shouldBe 100
    }

    @Test
    fun `?????? ?????? ??? ?????? ???????????? ????????? ????????? ??????????????????, ????????? null ??? ??????`() {
        val memberId = memberService.createMemberByAdministrator(
                CreateMemberRequest(
                        "?????????",
                        "??????",
                        "kth990303@naver.com",
                        PHONE_NUMBER,
                        mutableListOf(14, 19, 22),
                        "?????????",
                        "?????????",
                        "?????????",
                )
        )

        val currentGenerationMember = generationMemberRepository.findByGenerationAndMemberId(14, memberId)

        currentGenerationMember shouldNotBe null
        currentGenerationMember?.position shouldBe Position.DEVELOPER
        currentGenerationMember?.subPosition shouldBe SubPosition.BE
        currentGenerationMember?.score shouldBe null
    }

    @Test
    fun `?????? ?????? ??? ????????? ???????????? ?????? ????????? ???????????? ????????? ??????`() {
        val memberId = memberService.createMemberByAdministrator(
                CreateMemberRequest(
                        "?????????",
                        "??????",
                        "kth990303@naver.com",
                        PHONE_NUMBER,
                        mutableListOf(14, 19, 22),
                        "?????????",
                        "?????????",
                        "?????????",
                )
        )

        val currentGenerationMember = generationMemberRepository.findByGenerationAndMemberId(999, memberId)

        currentGenerationMember shouldBe null
    }

    @Test
    fun `?????? ?????? ????????? ????????? ???????????? ?????? ?????? ??????`() {
        initGenerationsAndSessions()
        val memberId = memberService.createMemberByAdministrator(
                CreateMemberRequest(
                        "?????????",
                        "??????",
                        "kth990303@naver.com",
                        PHONE_NUMBER,
                        mutableListOf(14, 19, 22),
                        "?????????",
                        "?????????",
                        "?????????",
                )
        )
        val generationMember = generationMemberRepository.findByGenerationAndMemberId(22, memberId)
        val attendances = attendanceRepository.findAll().filter { it.generationMemberId == generationMember!!.id }
        attendances.size shouldBe 8
        attendances.forEach {
            it.attendanceStatus shouldBe PENDING
        }
    }

    @Test
    fun `?????? ?????? ????????? ?????? ????????? ????????? ????????? ?????? ?????? ?????? ??????`() {
        val generation = 22
        val excelInput = createExcelInput()

        memberService.createGenerationMembers(generation, excelInput)
        memberRepository.flush()
        generationMemberRepository.flush()

        val actualMemberIds = memberRepository.findAll().map { it.id }
        val actualGenerationMembers = generationMemberRepository.findAllByMemberIdIn(actualMemberIds)
        actualMemberIds shouldHaveSize 3
        actualGenerationMembers shouldHaveSize 3
    }

    @Test
    fun `?????? ?????? ????????? ???????????? ???????????? ?????? ????????? ?????? ????????? ???????????? ????????? ???????????????`() {
        val generation = 22
        val excelInput = createExcelInput()
        val existingMember = memberRepository.save(createNewMember(email = "jinwoo@gmail.com", name = "????????????"))
        memberRepository.save(createNewMember(email = "not@matching.email"))
        memberService.createGenerationMembers(generation, excelInput)
        memberRepository.flush()
        generationMemberRepository.flush()

        memberRepository.findByEmail(existingMember.email)?.name shouldBe "?????????"
        memberRepository.findAll() shouldHaveSize 4
        generationMemberRepository.findAll() shouldHaveSize 3
    }

    @Test
    fun `?????? ?????? ????????? ???????????? ?????? ????????? ???????????? ?????? ????????? ??????????????? ?????? ????????? ????????? ???????????????`() {
        val generation = 22
        val excelInput = createExcelInput()
        val existingMember = memberRepository.save(createNewMember(email = "jinwoo@gmail.com"))
        val existingMatchingGenerationMember = generationMemberRepository.save(
                createNewGenerationMember(memberId = existingMember.id, generation = generation, position = Position.NULL)
        )
        generationMemberRepository.save(createNewGenerationMember(memberId = existingMember.id, generation = 99999))
        memberService.createGenerationMembers(generation, excelInput)
        memberRepository.flush()
        generationMemberRepository.flush()

        generationMemberRepository.findByIdOrNull(existingMatchingGenerationMember.id)?.position shouldBe Position.DEVELOPER
        memberRepository.findAll() shouldHaveSize 3
        generationMemberRepository.findAll() shouldHaveSize 4
    }

    @Test
    fun `?????? ?????? ????????? ?????? ????????? ???????????? ?????? ?????? ??????`() {
        initGenerationsAndSessions()
        val excelInput = createExcelInput()
        memberService.createGenerationMembers(generation = 22, excelInput)

        val attendances = attendanceRepository.findAll()

        attendances.size shouldBe 8 * 3
        attendances.forEach {
            it.attendanceStatus shouldBe PENDING
        }
    }

    @Test
    fun `?????? ?????? ????????? ?????? ????????? ?????????????????? ???????????? ????????? ?????? ?????? ????????? ????????? ???????????? ?????????`() {
        val generation = 22
        initGenerationsAndSessions()
        val excelInput = createExcelInput()
        val existingMember = memberRepository.save(createNewMember(email = "jinwoo@gmail.com"))
        val generationMember = generationMemberRepository.save(createNewGenerationMember(memberId = existingMember.id, generation = 22))
        sessionRepository.findAll().forEach {
            if (it.generation == generation) {
                attendanceRepository.save(createNewAttendance(sessionId = it.id, generationMemberId = generationMember.id, attendanceStatus = ATTENDED))
            }
        }
        memberService.createGenerationMembers(generation, excelInput)

        val attendances = attendanceRepository.findAll().filter { it.generationMemberId == generationMember.id }
        attendances.size shouldBe 8
        attendances.forEach {
            it.attendanceStatus shouldBe ATTENDED
        }
    }

    private fun initGenerationsAndSessions() {
        generationRepository.save(Generation(21))
        generationRepository.save(Generation(22))
        for (i in 1..8) {
            sessionRepository.save(createNewSession(generation = 21, week = i))
            sessionRepository.save(createNewSession(generation = 22, week = i))
        }
    }

    @Test
    fun `?????? ?????? ??????`() {
        val member1: Member = memberRepository.save(createNewMember())
        val member2: Member = memberRepository.save(createNewMember(name = "?????????", email = "kth990303@naver.com"))
        val generationMember1: GenerationMember = createNewGenerationMember(memberId = member1.id, generation = 22)
        val generationMember2: GenerationMember = createNewGenerationMember(memberId = member2.id, generation = 15)
        generationMemberRepository.save(generationMember1)
        generationMemberRepository.save(generationMember2)

        val actual = memberService.findAllByAdministrator()

        actual.data shouldHaveSize 2
    }

    @Test
    fun `?????? ?????? ??????`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)
        generationMemberRepository.save(generationMember)

        memberService.updateMemberByAdministrator(
                member.id,
                createUpdateMemberRequest()
        )

        val findMember = memberRepository.findByEmail(member.email)
                ?: throw NotFoundException.memberNotFound()
        findMember.name shouldBe "?????????"
    }

    @Test
    fun `?????? ?????? ?????? ??? ?????? ????????? ???????????? ????????? ??????`() {
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
    fun `?????? ?????? ?????? ??? ?????? ????????? ???????????? ????????? ??????`() {
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
    fun `?????? ???????????? ??????`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)
        generationMemberRepository.save(generationMember)

        memberService.updateStatusByAdministrator(member.id, "??????")

        val findMember = memberRepository.findByEmail(member.email)
                ?: throw NotFoundException.memberNotFound()

        findMember.status shouldBe MemberStatus.CERTIFICATED
    }

    @Test
    fun `?????? ?????? ??????`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)
        generationMemberRepository.save(generationMember)

        memberService.updatePositionByAdministrator(member.id, "????????????", null)

        val generations = generationMemberRepository.findAllByMemberId(member.id)
        generations[0].position shouldBe Position.DESIGNER
    }

    @Test
    fun `?????? ???????????? ??????`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)
        generationMemberRepository.save(generationMember)

        memberService.updatePassword(member, "2345")

        member.password shouldBe Password("2345")
    }

    @Test
    fun `?????? ???????????? ?????? ?????? ??????`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)
        generationMemberRepository.save(generationMember)

        val findMember = memberService.getByEmail(member.email)

        findMember.email shouldBe member.email
    }

    @Test
    fun `?????? ??????`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)
        generationMemberRepository.save(generationMember)

        memberService.deleteByAdministrator(member.id)

        memberRepository.findByIdOrNull(member.id) shouldBe null
    }

    @Test
    fun `?????? ?????? ??? ?????? ???????????? ?????? ?????? ??????`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id)
        generationMemberRepository.save(generationMember)

        memberService.deleteByAdministrator(member.id)

        val generations = generationMemberRepository.findAllByMemberId(member.id)
        generations shouldHaveSize 0
    }

    @Test
    fun `??? ?????? ??????`() {
        val member: Member = memberRepository.save(createNewMember(name = "?????????"))
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 22, position = Position.DEVELOPER)
        generationMemberRepository.save(generationMember)

        val profile = memberService.getProfile(member)

        profile.name shouldBe "?????????"
        profile.generation shouldBe 22
        profile.position shouldBe Position.DEVELOPER.value
    }

    @Test
    fun `??? ??? ????????? ?????? ????????? ?????? ????????? ??? ?????? ????????? ?????? ?????? ?????? ??????`() {
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
    fun `?????? ????????? ?????? ????????? ??? ?????? ????????? 0?????? ??????`() {
        val member: Member = memberRepository.save(createNewMember())

        val profile = memberService.getProfile(member)

        profile.generation shouldBe 0
        profile.position shouldBe ""
    }
}
