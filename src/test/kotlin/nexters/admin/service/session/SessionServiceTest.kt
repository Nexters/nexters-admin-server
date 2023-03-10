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
    fun `μΈμ μμ±`() {
        val id = sessionService.createSession(
                CreateSessionRequest(
                        title = "Test title",
                        description = "Test description",
                        generation = 22,
                        sessionDate = LocalDate.now(),
                        week = 3,
                )
        )

        val found = sessionRepository.findByIdOrNull(id)

        found shouldNotBe null
        found?.title shouldBe "Test title"
    }

    @Test
    fun `nullμ ν¬ν¨ν μΈμ μμ±`() {
        val id = sessionService.createSession(
                CreateSessionRequest(
                        title = "Test title",
                        description = null,
                        generation = 22,
                        sessionDate = LocalDate.now(),
                        week = 3,
                )
        )

        val found = sessionRepository.findByIdOrNull(id)

        found shouldNotBe null
        found?.title shouldBe "Test title"
        found?.description shouldBe null
    }

    @Test
    fun `μΈμ μμ±μ κΈ°μ νμλ€μ PENDING μν μΆμ μ λ³΄λ₯Ό μμ±νλ€`() {
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
                        sessionDate = LocalDate.now(),
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
    fun `μΈμ μ‘°ν`() {
        val session = createNewSession(title = "Test title")
        sessionRepository.save(session)

        val found = sessionService.findSession(session.id)

        found.title shouldBe "Test title"
    }

    @Test
    fun `νΉμ  κΈ°μμ μΈμ μ‘°ν`() {
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
    fun `νΉμ  κΈ°μμ μΈμμ΄ μ‘΄μ¬νμ§ μμ λ μΈμ μ‘°νλ₯Ό νλ©΄ λΉ λ°°μ΄μ λ°ννλ€`() {
        val session = createNewSession(generation = 23)
        sessionRepository.save(session)

        val founds = sessionService.findSessionByGeneration(22)

        founds shouldNotBe null
    }

    @Test
    fun `μΈμ μμ `() {
        val session = createNewSession()
        sessionRepository.save(session)
        sessionService.updateSession(session.id, UpdateSessionRequest(
                title = "Updated Title",
                description = "Test description",
                generation = 22,
                sessionDate = LocalDate.now(),
                week = 3,
        ))

        val found = sessionRepository.findByIdOrNull(session.id)

        found shouldNotBe null
        found?.title shouldBe "Updated Title"
        found?.startAttendTime shouldBe session.startAttendTime
    }

    @Test
    fun `μΈμ μ­μ `() {
        val session = createNewSession()
        sessionRepository.save(session)

        sessionService.deleteSession(session.id)

        val found = sessionRepository.findByIdOrNull(session.id)

        found shouldBe null
    }

    @Test
    fun `μΈμ ν μ‘°νμ ν΄λΉ κΈ°μ μΈμμ€ κ°μ₯ κ°κΉμ΄ λ€κ°μ€λ λ μ§μ μΈμμ΄ μ‘°νλλ€`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 22)
        generationMemberRepository.save(generationMember)
        val sessions = generateSessions()
        generateAttendances(sessions, generationMember)

        val actual = sessionService.getSessionHome(member)

        actual.data shouldNotBe null
        actual.data?.title shouldBe "λ€κ°μ€λ μΈμ"
    }

    @Test
    fun `μΈμ ν μ‘°νμ μΈμμ΄ μ‘΄μ¬νμ§ μμΌλ©΄ null λ°μ΄ν°λ₯Ό λ°ννλ€`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 22)
        generationMemberRepository.save(generationMember)

        val actual = sessionService.getSessionHome(member)

        actual.data shouldBe null
    }

    @Test
    fun `μΈμ ν μ‘°νμ μΆμμ΄ μμνμ§ μμ μνλ©΄ μΈμμνλ PENDING μ΄λ€`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 22)
        generationMemberRepository.save(generationMember)
        val sessions = generateSessions()
        val pendingSession: Session = createNewSession(
                title = "PENDING μΈμ",
                generation = 22,
                sessionDate = LocalDate.now(),
                startAttendTime = null,
                endAttendTime = null)
        sessionRepository.save(pendingSession)
        sessions.add(pendingSession)
        generateAttendances(sessions, generationMember)

        val actual = sessionService.getSessionHome(member)

        actual.data shouldNotBe null
        actual.data?.title shouldBe "PENDING μΈμ"
        actual.data?.sessionStatus shouldBe SessionStatus.PENDING
    }

    @Test
    fun `μΈμ ν μ‘°νμ μΆμμ§νμ€μΈ μΈμμνλ ONGOING μ΄λ€`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 22)
        generationMemberRepository.save(generationMember)
        val sessions = generateSessions()
        val ongoingSession: Session = createNewSession(
                title = "ONGOING μΈμ",
                generation = 22,
                sessionDate = LocalDate.now(),
                startAttendTime = LocalDateTime.now().minusMinutes(3),
                endAttendTime = null)
        sessionRepository.save(ongoingSession)
        sessions.add(ongoingSession)
        generateAttendances(sessions, generationMember)

        val actual = sessionService.getSessionHome(member)

        actual.data shouldNotBe null
        actual.data?.title shouldBe "ONGOING μΈμ"
        actual.data?.sessionStatus shouldBe SessionStatus.ONGOING
    }

    @Test
    fun `μΈμ ν μ‘°νμ μΆμμλ£λ μΈμμνλ EXPIRED μ΄λ€`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 22)
        generationMemberRepository.save(generationMember)
        val sessions = generateSessions()
        val expiredSession: Session = createNewSession(
                title = "EXPIRED μΈμ",
                generation = 22,
                sessionDate = LocalDate.now(),
                startAttendTime = LocalDateTime.now().minusMinutes(10),
                endAttendTime = LocalDateTime.now().minusMinutes(3))
        sessionRepository.save(expiredSession)
        sessions.add(expiredSession)
        generateAttendances(sessions, generationMember)

        val actual = sessionService.getSessionHome(member)

        actual.data shouldNotBe null
        actual.data?.title shouldBe "EXPIRED μΈμ"
        actual.data?.sessionStatus shouldBe SessionStatus.EXPIRED
    }

    private fun generateSessions(): MutableList<Session> {
        val today: LocalDate = LocalDate.now()
        val session1: Session = createNewSession(generation = 22, sessionDate = today.minusDays(3))
        val session2: Session = createNewSession(generation = 22, sessionDate = today.plusDays(10))
        val session3: Session = createNewSession(generation = 22, sessionDate = today.plusDays(3), title = "λ€κ°μ€λ μΈμ")
        val session4: Session = createNewSession(generation = 23, sessionDate = today.plusDays(1))

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
