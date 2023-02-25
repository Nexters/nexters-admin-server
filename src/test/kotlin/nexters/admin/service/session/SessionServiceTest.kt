package nexters.admin.service.session

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nexters.admin.controller.session.CreateSessionRequest
import nexters.admin.controller.session.UpdateSessionRequest
import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.session.Session
import nexters.admin.domain.session.SessionStatus
import nexters.admin.domain.user.member.Member
import nexters.admin.repository.AttendanceRepository
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.MemberRepository
import nexters.admin.repository.SessionRepository
import nexters.admin.repository.findAllPendingAttendanceOf
import nexters.admin.testsupport.ApplicationTest
import nexters.admin.testsupport.createNewAttendance
import nexters.admin.testsupport.createNewGenerationMember
import nexters.admin.testsupport.createNewMember
import nexters.admin.testsupport.createNewSession
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import java.time.LocalDateTime

@ApplicationTest
class SessionServiceTest(
        @Autowired private val sessionService: SessionService,
        @Autowired private val sessionRepository: SessionRepository,
        @Autowired private val memberRepository: MemberRepository,
        @Autowired private val generationMemberRepository: GenerationMemberRepository,
        @Autowired private val attendanceRepository: AttendanceRepository,
) {

    @Test
    fun `세션 생성`() {
        val id = sessionService.createSession(
                CreateSessionRequest(
                        title = "Test title",
                        description = "Test description",
                        generation = 22,
                        sessionTime = LocalDate.now(),
                        week = 3,
                )
        )

        val found = sessionRepository.findByIdOrNull(id)

        found shouldNotBe null
        found?.title shouldBe "Test title"
    }

    @Test
    fun `null을 포함한 세션 생성`() {
        val id = sessionService.createSession(
                CreateSessionRequest(
                        title = "Test title",
                        description = null,
                        generation = 22,
                        sessionTime = LocalDate.now(),
                        week = 3,
                )
        )

        val found = sessionRepository.findByIdOrNull(id)

        found shouldNotBe null
        found?.title shouldBe "Test title"
        found?.description shouldBe null
    }

    @Test
    fun `세션 생성시 기수 회원들의 PENDING 상태 출석 정보를 생성한다`() {
        val member1 = createNewMember()
        val member2 = createNewMember()
        memberRepository.saveAll(listOf(member1, member2))
        val generationMember1 = createNewGenerationMember(memberId = member1.id, generation = 22)
        val generationMember2 = createNewGenerationMember(memberId = member2.id, generation = 22)
        val generationMember3 = createNewGenerationMember(memberId = member2.id, generation = 23)
        generationMemberRepository.saveAll(listOf(generationMember1, generationMember2, generationMember3))
        val savedSessionId = sessionService.createSession(
                CreateSessionRequest(
                        title = "Test title",
                        description = null,
                        generation = 22,
                        sessionTime = LocalDate.now(),
                        week = 3,
                )
        )

        val attendances = attendanceRepository.findAllPendingAttendanceOf(sessionId = savedSessionId)
        attendances.size shouldBe 2
        attendances.forEach {
            it.sessionId shouldBe savedSessionId
        }
    }

    @Test
    fun `세션 조회`() {
        val session = createNewSession(title = "Test title")
        sessionRepository.save(session)

        val found = sessionService.findSession(session.id)

        found.title shouldBe "Test title"
    }

    @Test
    fun `특정 기수의 세션 조회`() {
        val session1 = createNewSession(title = "Test title1", generation = 22)
        val session2 = createNewSession(title = "Test title2", generation = 22)
        val session3 = createNewSession(title = "Test title3", generation = 23)
        sessionRepository.save(session1)
        sessionRepository.save(session2)
        sessionRepository.save(session3)

        val founds = sessionService.findSessionByGeneration(22)

        founds.data shouldHaveSize 2
        founds.data.forEach {
            it.generation shouldBe 22
        }
    }

    @Test
    fun `특정 기수에 세션이 존재하지 않을 때 세션 조회를 하면 빈 배열을 반환한다`() {
        val session = createNewSession(generation = 23)
        sessionRepository.save(session)

        val founds = sessionService.findSessionByGeneration(22)

        founds shouldNotBe null
    }

    @Test
    fun `세션 수정`() {
        val session = createNewSession()
        sessionRepository.save(session)
        sessionService.updateSession(session.id, UpdateSessionRequest(
                title = "Updated Title",
                description = "Test description",
                generation = 22,
                sessionTime = LocalDate.now(),
                week = 3,
        ))

        val found = sessionRepository.findByIdOrNull(session.id)

        found shouldNotBe null
        found?.title shouldBe "Updated Title"
        found?.startAttendTime shouldBe session.startAttendTime
    }

    @Test
    fun `세션 삭제`() {
        val session = createNewSession()
        sessionRepository.save(session)

        sessionService.deleteSession(session.id)

        val found = sessionRepository.findByIdOrNull(session.id)

        found shouldBe null
    }

    @Test
    fun `세션 홈 조회시 해당 기수 세션중 가장 가까운 다가오는 날짜의 세션이 조회된다`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 22)
        generationMemberRepository.save(generationMember)
        val sessions = generateSessions()
        generateAttendances(sessions, generationMember)

        val actual = sessionService.getSessionHome(member)

        actual.data shouldNotBe null
        actual.data?.title shouldBe "다가오는 세션"
    }

    @Test
    fun `세션 홈 조회시 세션이 존재하지 않으면 null 데이터를 반환한다`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 22)
        generationMemberRepository.save(generationMember)

        val actual = sessionService.getSessionHome(member)

        actual.data shouldBe null
    }

    @Test
    fun `세션 홈 조회시 출석이 시작하지 않은 상태면 세션상태는 PENDING 이다`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 22)
        generationMemberRepository.save(generationMember)
        val sessions = generateSessions()
        val pendingSession: Session = createNewSession(
                title = "PENDING 세션",
                generation = 22,
                sessionTime = LocalDate.now(),
                startAttendTime = null,
                endAttendTime = null)
        sessionRepository.save(pendingSession)
        sessions.add(pendingSession)
        generateAttendances(sessions, generationMember)

        val actual = sessionService.getSessionHome(member)

        actual.data shouldNotBe null
        actual.data?.title shouldBe "PENDING 세션"
        actual.data?.sessionStatus shouldBe SessionStatus.PENDING
    }

    @Test
    fun `세션 홈 조회시 출석진행중인 세션상태는 ONGOING 이다`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 22)
        generationMemberRepository.save(generationMember)
        val sessions = generateSessions()
        val ongoingSession: Session = createNewSession(
                title = "ONGOING 세션",
                generation = 22,
                sessionTime = LocalDate.now(),
                startAttendTime = LocalDateTime.now().minusMinutes(3),
                endAttendTime = null)
        sessionRepository.save(ongoingSession)
        sessions.add(ongoingSession)
        generateAttendances(sessions, generationMember)

        val actual = sessionService.getSessionHome(member)

        actual.data shouldNotBe null
        actual.data?.title shouldBe "ONGOING 세션"
        actual.data?.sessionStatus shouldBe SessionStatus.ONGOING
    }

    @Test
    fun `세션 홈 조회시 출석완료된 세션상태는 EXPIRED 이다`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 22)
        generationMemberRepository.save(generationMember)
        val sessions = generateSessions()
        val expiredSession: Session = createNewSession(
                title = "EXPIRED 세션",
                generation = 22,
                sessionTime = LocalDate.now(),
                startAttendTime = LocalDateTime.now().minusMinutes(10),
                endAttendTime = LocalDateTime.now().minusMinutes(3))
        sessionRepository.save(expiredSession)
        sessions.add(expiredSession)
        generateAttendances(sessions, generationMember)

        val actual = sessionService.getSessionHome(member)

        actual.data shouldNotBe null
        actual.data?.title shouldBe "EXPIRED 세션"
        actual.data?.sessionStatus shouldBe SessionStatus.EXPIRED
    }

    private fun generateSessions(): MutableList<Session> {
        val today: LocalDate = LocalDate.now()
        val session1: Session = createNewSession(generation = 22, sessionTime = today.minusDays(3))
        val session2: Session = createNewSession(generation = 22, sessionTime = today.plusDays(10))
        val session3: Session = createNewSession(generation = 22, sessionTime = today.plusDays(3), title = "다가오는 세션")
        val session4: Session = createNewSession(generation = 23, sessionTime = today.plusDays(1))

        sessionRepository.save(session1)
        sessionRepository.save(session2)
        sessionRepository.save(session3)
        sessionRepository.save(session4)

        return mutableListOf(session1, session2, session3, session4)
    }

    private fun generateAttendances(sessions: List<Session>, generationMember: GenerationMember) {
        for (session in sessions) {
            attendanceRepository.save(createNewAttendance(sessionId = session.id, generationMemberId = generationMember.id))
        }
    }

}
