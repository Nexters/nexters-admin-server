package nexters.admin.service.attendance

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.generation_member.MAX_SCORE
import nexters.admin.domain.session.Session
import nexters.admin.domain.user.member.Member
import nexters.admin.exception.BadRequestException
import nexters.admin.repository.AttendanceRepository
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.GenerationRepository
import nexters.admin.repository.MemberRepository
import nexters.admin.repository.QrCodeRepository
import nexters.admin.repository.SessionRepository
import nexters.admin.testsupport.ApplicationTest
import nexters.admin.testsupport.createNewAttendance
import nexters.admin.testsupport.createNewGeneration
import nexters.admin.testsupport.createNewGenerationMember
import nexters.admin.testsupport.createNewMember
import nexters.admin.testsupport.createNewSession
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional

@Transactional
@ApplicationTest
class AttendanceServiceTest(
        @Autowired private val attendanceService: AttendanceService,
        @Autowired private val attendanceRepository: AttendanceRepository,
        @Autowired private val generationMemberRepository: GenerationMemberRepository,
        @Autowired private val sessionRepository: SessionRepository,
        @Autowired private val memberRepository: MemberRepository,
        @Autowired private val qrCodeRepository: QrCodeRepository,
        @Autowired private val generationRepository: GenerationRepository,
) {
    @Test
    fun `??? ???????????? ??????`() {
        generationRepository.save(createNewGeneration())
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = generationMemberRepository
                .save(createNewGenerationMember(memberId = member.id))
        val session1: Session = sessionRepository.save(createNewSession())
        val session2: Session = sessionRepository.save(createNewSession(week = 2))
        generateAttendance(session1, generationMember, AttendanceStatus.ATTENDED)
        generateAttendance(session2, generationMember, AttendanceStatus.TARDY)

        val attendanceProfile = attendanceService.getAttendanceProfile(member)

        attendanceProfile.isGenerationMember shouldBe true
        attendanceProfile.attendanceData!!.run {
            score shouldBe 100
            isCompletable shouldBe true
            attendances.run {
                size shouldBe 2
                map { it.attendanceStatus } shouldContainExactlyInAnyOrder listOf(AttendanceStatus.ATTENDED, AttendanceStatus.TARDY)
            }
        }
    }

    @Test
    fun `??? ???????????? ????????? PENDING??? ?????? ???????????? ????????????`() {
        generationRepository.save(createNewGeneration())
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = generationMemberRepository
                .save(createNewGenerationMember(memberId = member.id))
        saveAttendanceDataWithStatuses(generationMember)

        val attendanceProfile = attendanceService.getAttendanceProfile(member)
        attendanceProfile.attendanceData!!.attendances.run {
            size shouldBe 4
            map { it.attendanceStatus } shouldNotContain AttendanceStatus.PENDING
        }
    }

    private fun saveAttendanceDataWithStatuses(generationMember: GenerationMember) {
        val session1: Session = sessionRepository.save(createNewSession())
        val session2: Session = sessionRepository.save(createNewSession(week = 2))
        val session3: Session = sessionRepository.save(createNewSession(week = 3))
        val session4: Session = sessionRepository.save(createNewSession(week = 4))
        val pendingSession: Session = sessionRepository.save(createNewSession(week = 5))
        generateAttendance(session1, generationMember, AttendanceStatus.ATTENDED)
        generateAttendance(session2, generationMember, AttendanceStatus.TARDY)
        generateAttendance(session3, generationMember, AttendanceStatus.UNAUTHORIZED_ABSENCE)
        generateAttendance(session4, generationMember, AttendanceStatus.AUTHORIZED_ABSENCE)
        generateAttendance(pendingSession, generationMember, AttendanceStatus.PENDING)
    }

    @Test
    fun `??? ???????????? ????????? ?????? ????????? ????????? ????????? ???????????? ????????? false???, ???????????? null??? ????????????`() {
        generationRepository.save(createNewGeneration())
        val member: Member = memberRepository.save(createNewMember())

        val attendanceProfile = attendanceService.getAttendanceProfile(member)

        attendanceProfile.isGenerationMember shouldBe false
        attendanceProfile.attendanceData shouldBe null
    }

    @Test
    fun `??? ???????????? ????????? week ????????? ?????????????????? ????????????`() {
        generationRepository.save(createNewGeneration())
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = generationMemberRepository
                .save(createNewGenerationMember(memberId = member.id))
        saveUnorderedAttendanceData(generationMember)

        val attendanceProfile = attendanceService.getAttendanceProfile(member)

        attendanceProfile.attendanceData!!.attendances shouldBeSortedWith { a, b -> b.week.compareTo(a.week) }
    }

    private fun saveUnorderedAttendanceData(generationMember: GenerationMember) {
        val session1: Session = sessionRepository.save(createNewSession(week = 1))
        val session2: Session = sessionRepository.save(createNewSession(week = 4))
        val session3: Session = sessionRepository.save(createNewSession(week = 2))
        val session4: Session = sessionRepository.save(createNewSession(week = 3))
        generateAttendance(session1, generationMember)
        generateAttendance(session2, generationMember)
        generateAttendance(session3, generationMember)
        generateAttendance(session4, generationMember)
    }

    @ParameterizedTest
    @EnumSource(AttendanceStatus::class, mode = EXCLUDE, names = ["PENDING"])
    fun `??? ???????????? ????????? ??????????????? ?????? ????????? ????????????`(attendanceStatus: AttendanceStatus) {
        generationRepository.save(createNewGeneration())
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = generationMemberRepository
                .save(createNewGenerationMember(memberId = member.id))
        val session: Session = sessionRepository.save(createNewSession())
        generateAttendance(session, generationMember, attendanceStatus)

        val attendanceProfile = attendanceService.getAttendanceProfile(member)

        attendanceProfile.attendanceData!!.attendances.getOrNull(0)!!.penaltyScore shouldBe attendanceStatus.penaltyScore
    }

    private fun generateAttendance(
            session: Session,
            generationMember: GenerationMember,
            attendanceStatus: AttendanceStatus) = attendanceRepository.save(
            createNewAttendance(
                    sessionId = session.id,
                    generationMemberId = generationMember.id,
                    attendanceStatus = attendanceStatus
            )
    )

    private fun generateAttendance(
            session: Session,
            generationMember: GenerationMember) = attendanceRepository.save(
            createNewAttendance(
                    sessionId = session.id,
                    generationMemberId = generationMember.id,
            )
    )

    @Test
    fun `????????? QR ????????? ?????? ????????? ?????? ?????? ??? ?????? ??????`() {
        generationRepository.save(createNewGeneration())
        val member = memberRepository.save(createNewMember())
        val generationMember = generationMemberRepository
                .save(createNewGenerationMember(memberId = member.id))
        val session = sessionRepository.save(createNewSession())
        val attendance = generateAttendance(session, generationMember, AttendanceStatus.PENDING)
        qrCodeRepository.initializeCodes(session.id, AttendanceStatus.TARDY)
        val validCode = qrCodeRepository.findCurrentValidCode()!!

        attendanceService.attendWithQrCode(member, validCode.value)

        attendance.attendanceStatus shouldBe AttendanceStatus.TARDY
        attendance.scoreChanged shouldBe AttendanceStatus.TARDY.penaltyScore
        generationMember.score shouldBe MAX_SCORE + AttendanceStatus.TARDY.penaltyScore
    }

    @Test
    fun `?????? ?????? ?????? ????????? PENDING ????????? ????????? ?????? ?????? ???????????? ??????`() {
        generationRepository.save(createNewGeneration())
        val member = memberRepository.save(createNewMember())
        val generationMember = generationMemberRepository
                .save(createNewGenerationMember(memberId = member.id))
        val session = sessionRepository.save(createNewSession())
        val attendance = generateAttendance(session, generationMember, AttendanceStatus.PENDING)
        qrCodeRepository.initializeCodes(session.id, AttendanceStatus.TARDY)

        attendanceService.endAttendance()

        val actual = attendanceRepository.findByIdOrNull(attendance.id)
        actual?.attendanceStatus shouldBe AttendanceStatus.UNAUTHORIZED_ABSENCE
        generationMember.score shouldBe MAX_SCORE + AttendanceStatus.UNAUTHORIZED_ABSENCE.penaltyScore
        qrCodeRepository.getQrCodes() shouldHaveSize 0
    }

    @Test
    fun `????????? QR ????????? ?????? ?????????, ?????? ??????`() {
        generationRepository.save(createNewGeneration())
        val member = memberRepository.save(createNewMember())
        qrCodeRepository.initializeCodes(1L, AttendanceStatus.ATTENDED)

        shouldThrow<BadRequestException> {
            attendanceService.attendWithQrCode(member, "INVALIDCODE")
        }
    }

    @Test
    fun `?????? ??????????????? ?????? ????????? QR ????????? ?????? ?????????, ?????? ??????`() {
        generationRepository.save(createNewGeneration())
        val member = memberRepository.save(createNewMember())
        val session = sessionRepository.save(createNewSession())
        qrCodeRepository.initializeCodes(session.id, AttendanceStatus.TARDY)
        val validCode = qrCodeRepository.findCurrentValidCode()!!

        shouldThrow<BadRequestException> {
            attendanceService.attendWithQrCode(member, validCode.value)
        }
    }

    @Test
    fun `???????????? ?????? ????????? ???????????? ?????? ?????? ?????? ????????? ??????????????? ???????????? ?????? ??????`() {
        val generationMember = generationMemberRepository.save(createNewGenerationMember())
        attendanceRepository.save(createNewAttendance(
                generationMemberId = generationMember.id,
                sessionId = 1L,
                attendanceStatus = AttendanceStatus.TARDY
        ))
        val currentAttendance =  attendanceRepository.save(createNewAttendance(
                generationMemberId = generationMember.id,
                sessionId = 2L,
                attendanceStatus = AttendanceStatus.UNAUTHORIZED_ABSENCE
        ))

        attendanceService.updateAttendanceStatusByAdministrator(currentAttendance.id, AttendanceStatus.TARDY.name, "???????????? ??????")

        currentAttendance.scoreChanged shouldBe AttendanceStatus.TARDY.penaltyScore
        currentAttendance.note shouldBe "???????????? ??????"
        generationMember.score shouldBe MAX_SCORE + (AttendanceStatus.TARDY.penaltyScore * 2)
    }

    @Test
    fun `???????????? ???????????? ?????? ?????? ?????? ?????? ????????? ??????????????? ???????????? ?????? ??????`() {
        val generationMember = generationMemberRepository.save(createNewGenerationMember())
        attendanceRepository.save(createNewAttendance(
                generationMemberId = generationMember.id,
                sessionId = 1L,
                attendanceStatus = AttendanceStatus.TARDY
        ))
        val currentAttendance = attendanceRepository.save(createNewAttendance(
                generationMemberId = generationMember.id,
                sessionId = 2L,
                attendanceStatus = AttendanceStatus.ATTENDED
        ))

        attendanceService.addExtraAttendanceScoreByAdministrator(currentAttendance.id, 10, "???????????? ??? ???")

        currentAttendance.scoreChanged shouldBe 10
        currentAttendance.extraScoreNote shouldBe "???????????? ??? ???"
        generationMember.score shouldBe MAX_SCORE + AttendanceStatus.TARDY.penaltyScore + 10
    }

    fun `?????? ????????? ?????? ?????? ???????????? ?????? ??????`() {
        generationRepository.save(createNewGeneration(generation = 13))
        generationRepository.save(createNewGeneration(generation = 20))
        generationRepository.save(createNewGeneration(generation = 22))
        val member1 = memberRepository.save(createNewMember(name = "?????????"))
        generationMemberRepository.save(createNewGenerationMember(memberId = member1.id, generation = 20))
        val generationMember1 = generationMemberRepository.save(createNewGenerationMember(memberId = member1.id, generation = 22))
        val member2 = memberRepository.save(createNewMember(name = "?????????"))
        val generationMember2 = generationMemberRepository.save(createNewGenerationMember(memberId = member2.id, generation = 22))
        val member3 = memberRepository.save(createNewMember(name = "?????????"))
        val generationMember3 = generationMemberRepository.save(createNewGenerationMember(memberId = member3.id, generation = 22))
        val member4 = memberRepository.save(createNewMember(name = "?????????"))
        generationMemberRepository.save(createNewGenerationMember(memberId = member4.id, generation = 13))
        val generationMember4 = generationMemberRepository.save(createNewGenerationMember(memberId = member4.id, generation = 22))

        val session = sessionRepository.save(createNewSession())
        saveAttendances(session, generationMember1, generationMember2, generationMember3, generationMember4)

        val actual = attendanceService.findAttendancesBySessionId(session.id)

        actual.week shouldBe session.week
        actual.attended shouldBe 1
        actual.tardy shouldBe 1
        actual.absence shouldBe 1
        actual.data shouldHaveSize 4
        actual.data.map {
            it.initialGeneration
        } shouldContainExactly listOf(20, 22, 22, 13)
    }

    @Test
    fun `?????? ????????? ????????? ?????? ??? ?????? ????????? ?????? ?????? ???????????? ?????? ??????`() {
        generationRepository.save(createNewGeneration(generation = 13))
        generationRepository.save(createNewGeneration(generation = 20))
        generationRepository.save(createNewGeneration(generation = 22))
        val member1 = memberRepository.save(createNewMember(name = "?????????"))
        generationMemberRepository.save(createNewGenerationMember(memberId = member1.id, generation = 20))
        generationMemberRepository.save(createNewGenerationMember(memberId = member1.id, generation = 22))
        val member2 = memberRepository.save(createNewMember(name = "?????????"))
        generationMemberRepository.save(createNewGenerationMember(memberId = member2.id, generation = 22))
        val member3 = memberRepository.save(createNewMember(name = "?????????"))
        generationMemberRepository.save(createNewGenerationMember(memberId = member3.id, generation = 22))
        val member4 = memberRepository.save(createNewMember(name = "?????????"))
        generationMemberRepository.save(createNewGenerationMember(memberId = member4.id, generation = 13))
        generationMemberRepository.save(createNewGenerationMember(memberId = member4.id, generation = 22))

        val session = sessionRepository.save(createNewSession())

        val actual = attendanceService.findAttendancesBySessionId(session.id)

        actual.week shouldBe session.week
        actual.attended shouldBe 0
        actual.tardy shouldBe 0
        actual.absence shouldBe 0
        actual.data shouldNotBe null
        actual.data shouldHaveSize 0
    }

    @Test
    fun `?????? ?????? ?????? ??? ?????? ????????? ?????? ???????????? ????????? ????????????`() {
        generationRepository.save(createNewGeneration(generation = 13))
        generationRepository.save(createNewGeneration(generation = 20))
        generationRepository.save(createNewGeneration(generation = 22))
        val member1 = memberRepository.save(createNewMember(name = "?????????"))
        generationMemberRepository.save(createNewGenerationMember(memberId = member1.id, generation = 20))
        val generationMember1 = generationMemberRepository.save(createNewGenerationMember(memberId = member1.id, generation = 22))
        val member2 = memberRepository.save(createNewMember(name = "?????????"))
        val generationMember2 = generationMemberRepository.save(createNewGenerationMember(memberId = member2.id, generation = 22))
        val member3 = memberRepository.save(createNewMember(name = "?????????"))
        val generationMember3 = generationMemberRepository.save(createNewGenerationMember(memberId = member3.id, generation = 22))
        val member4 = memberRepository.save(createNewMember(name = "?????????"))
        generationMemberRepository.save(createNewGenerationMember(memberId = member4.id, generation = 13))
        val generationMember4 = generationMemberRepository.save(createNewGenerationMember(memberId = member4.id, generation = 22))

        val session = sessionRepository.save(createNewSession())
        saveAttendances(session, generationMember1, generationMember2, generationMember3, generationMember4)

        val actual = attendanceService.findAllActivities(22)
        actual.data shouldHaveSize 4
        actual.data.map {
            it.initialGeneration
        } shouldContainExactly listOf(20, 22, 22, 13)
    }

    private fun saveAttendances(session: Session, generationMember1: GenerationMember, generationMember2: GenerationMember, generationMember3: GenerationMember, generationMember4: GenerationMember) {
        attendanceRepository.save(createNewAttendance(
                generationMemberId = generationMember1.id,
                sessionId = session.id,
                attendanceStatus = AttendanceStatus.TARDY)
        )
        attendanceRepository.save(createNewAttendance(
                generationMemberId = generationMember2.id,
                sessionId = session.id,
                attendanceStatus = AttendanceStatus.ATTENDED)
        )
        attendanceRepository.save(createNewAttendance(
                generationMemberId = generationMember3.id,
                sessionId = session.id,
                attendanceStatus = AttendanceStatus.AUTHORIZED_ABSENCE)
        )
        attendanceRepository.save(createNewAttendance(
                generationMemberId = generationMember4.id,
                sessionId = session.id,
                attendanceStatus = AttendanceStatus.PENDING)
        )
    }

    @Test
    fun `?????? ?????? ????????? ?????? ?????? ??? ??????????????? ?????? ?????? ?????? ????????? ????????????`() {
        generationRepository.save(createNewGeneration(generation = 22))
        val member = memberRepository.save(createNewMember(name = "?????????"))
        val generationMember = generationMemberRepository.save(createNewGenerationMember(memberId = member.id, generation = 22))

        val session1 = sessionRepository.save(createNewSession())
        attendanceRepository.save(createNewAttendance(
                generationMemberId = generationMember.id,
                sessionId = session1.id,
                attendanceStatus = AttendanceStatus.TARDY)
        )

        val session2 = sessionRepository.save(createNewSession(week = 2))
        attendanceRepository.save(createNewAttendance(
                generationMemberId = generationMember.id,
                sessionId = session2.id,
                attendanceStatus = AttendanceStatus.ATTENDED)
        )

        val session3 = sessionRepository.save(createNewSession(week = 3))
        attendanceRepository.save(createNewAttendance(
                generationMemberId = generationMember.id,
                sessionId = session3.id,
                attendanceStatus = AttendanceStatus.UNAUTHORIZED_ABSENCE)
        )

        val actual = attendanceService.findActivityHistory(generationMember.id, 22)
        actual.data shouldHaveSize 3
        actual.data.map {
            it.attendanceStatus
        } shouldContainExactly listOf(
                AttendanceStatus.UNAUTHORIZED_ABSENCE.value,
                AttendanceStatus.ATTENDED.value,
                AttendanceStatus.TARDY.value
        )
    }
}
