package nexters.admin.service.attendance

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.generation_member.GenerationMember
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
    fun `내 출석정보 조회`() {
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
    fun `내 출석정보 조회시 PENDING이 아닌 정보들만 불러온다`() {
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
    fun `내 출석정보 조회시 해당 기수의 멤버가 아니면 기수확인 속성은 false을, 데이터는 null을 반환한다`() {
        generationRepository.save(createNewGeneration())
        val member: Member = memberRepository.save(createNewMember())

        val attendanceProfile = attendanceService.getAttendanceProfile(member)

        attendanceProfile.isGenerationMember shouldBe false
        attendanceProfile.attendanceData shouldBe null
    }

    @Test
    fun `내 출석정보 조회시 week 순으로 내림차순해서 반환한다`() {
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
    fun `내 출석정보 조회시 출석상태에 맞는 점수를 반환한다`(attendanceStatus: AttendanceStatus) {
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
    fun `유효한 QR 코드로 출석 성공시 출석 상태 수정`() {
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
    }

    @Test
    fun `출석 체크 종료 이후에 PENDING 상태의 출석은 모두 무단 결석으로 처리`() {
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
        qrCodeRepository.getQrCodes() shouldHaveSize 0
    }

    @Test
    fun `잘못된 QR 코드로 출석 시도시, 예외 발생`() {
        generationRepository.save(createNewGeneration())
        val member = memberRepository.save(createNewMember())
        qrCodeRepository.initializeCodes(1L, AttendanceStatus.ATTENDED)

        shouldThrow<BadRequestException> {
            attendanceService.attendWithQrCode(member, "INVALIDCODE")
        }
    }

    @Test
    fun `현재 활동기수가 아닌 회원이 QR 코드로 출석 시도시, 예외 발생`() {
        generationRepository.save(createNewGeneration())
        val member = memberRepository.save(createNewMember())
        val session = sessionRepository.save(createNewSession())
        qrCodeRepository.initializeCodes(session.id, AttendanceStatus.TARDY)
        val validCode = qrCodeRepository.findCurrentValidCode()!!

        shouldThrow<BadRequestException> {
            attendanceService.attendWithQrCode(member, validCode.value)
        }
    }
}
