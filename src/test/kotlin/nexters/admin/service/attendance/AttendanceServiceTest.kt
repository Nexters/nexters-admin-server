package nexters.admin.service.attendance

import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import nexters.admin.createNewAttendance
import nexters.admin.createNewGenerationMember
import nexters.admin.createNewMember
import nexters.admin.createNewSession
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.session.Session
import nexters.admin.domain.user.member.Member
import nexters.admin.repository.AttendanceRepository
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.MemberRepository
import nexters.admin.repository.SessionRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest
class AttendanceServiceTest(
        @Autowired private val attendanceRepository: AttendanceRepository,
        @Autowired private val generationMemberRepository: GenerationMemberRepository,
        @Autowired private val sessionRepository: SessionRepository,
        @Autowired private val memberRepository: MemberRepository
) {
    val attendanceService = AttendanceService(attendanceRepository, generationMemberRepository, sessionRepository)

    @AfterEach
    fun tearDown() {
        attendanceRepository.deleteAll()
        generationMemberRepository.deleteAll()
        generationMemberRepository.deleteAll()
    }

    @Test
    fun `내 출석정보 조회`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = generationMemberRepository
                .save(createNewGenerationMember(memberId = member.id))
        val session1: Session = sessionRepository.save(createNewSession())
        val session2: Session = sessionRepository.save(createNewSession(week = 2))
        attendanceRepository.save(
                createNewAttendance(
                        sessionId = session1.id,
                        generationMemberId = generationMember.id
                )
        )
        attendanceRepository.save(
                createNewAttendance(
                        sessionId = session2.id,
                        generationMemberId = generationMember.id,
                        attendanceStatus = AttendanceStatus.TARDY
                )
        )

        val attendanceProfile = attendanceService.getAttendanceProfile(member)

        attendanceProfile.isGenerationMember shouldBe true
        attendanceProfile.data!!.run {
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
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = generationMemberRepository
                .save(createNewGenerationMember(memberId = member.id))
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

        val attendanceProfile = attendanceService.getAttendanceProfile(member)
        attendanceProfile.data!!.attendances.run {
            size shouldBe 4
            map { it.attendanceStatus } shouldNotContain AttendanceStatus.PENDING
        }
    }

    @Test
    fun `내 출석정보 조회시 해당 기수의 멤버가 아니면 기수확인 속성은 false을, 데이터는 null을 반환한다`() {
        val member: Member = memberRepository.save(createNewMember())

        val attendanceProfile = attendanceService.getAttendanceProfile(member)

        attendanceProfile.isGenerationMember shouldBe false
        attendanceProfile.data shouldBe null
    }

    @Test
    fun `내 출석정보 조회시 week 순으로 내림차순해서 반환한다`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = generationMemberRepository
                .save(createNewGenerationMember(memberId = member.id))
        val session1: Session = sessionRepository.save(createNewSession(week = 1))
        val session2: Session = sessionRepository.save(createNewSession(week = 4))
        val session3: Session = sessionRepository.save(createNewSession(week = 2))
        val session4: Session = sessionRepository.save(createNewSession(week = 3))
        generateAttendance(session1, generationMember)
        generateAttendance(session2, generationMember)
        generateAttendance(session3, generationMember)
        generateAttendance(session4, generationMember)

        val attendanceProfile = attendanceService.getAttendanceProfile(member)

        attendanceProfile.data!!.attendances shouldBeSortedWith { a, b -> b.week.compareTo(a.week) }
    }

    @ParameterizedTest
    @EnumSource(AttendanceStatus::class, mode = EXCLUDE, names = ["PENDING"])
    fun `내 출석정보 조회시 출석상태에 맞는 점수를 반환한다`(attendanceStatus: AttendanceStatus) {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = generationMemberRepository
                .save(createNewGenerationMember(memberId = member.id))
        val session: Session = sessionRepository.save(createNewSession())
        generateAttendance(session, generationMember, attendanceStatus)

        val attendanceProfile = attendanceService.getAttendanceProfile(member)

        attendanceProfile.data!!.attendances.getOrNull(0)!!.score shouldBe attendanceStatus.score
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
}
