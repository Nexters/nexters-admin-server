package nexters.admin.service.session

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.session.Session
import nexters.admin.domain.user.member.Member
import nexters.admin.repository.AttendanceRepository
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.MemberRepository
import nexters.admin.repository.SessionRepository
import nexters.admin.testsupport.createNewAttendance
import nexters.admin.testsupport.createNewGenerationMember
import nexters.admin.testsupport.createNewMember
import nexters.admin.testsupport.createNewSession
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Transactional
@SpringBootTest
class SessionServiceTest(
        @Autowired private val sessionService: SessionService,
        @Autowired private val sessionRepository: SessionRepository,
        @Autowired private val memberRepository: MemberRepository,
        @Autowired private val generationMemberRepository: GenerationMemberRepository,
        @Autowired private val attendanceRepository: AttendanceRepository
) {

    @Test
    fun `세션 생성`() {
        val id = sessionService.createSession(
                CreateSessionRequest(
                        title = "Test title",
                        description = "Test description",
                        message = "Test message",
                        generation = 22,
                        sessionTime = LocalDate.now(),
                        week = 3,
                        startAttendTime = LocalDateTime.now(),
                        endAttendTime = LocalDateTime.now()
                )
        )

        val found = sessionRepository.findByIdOrNull(id)

        found shouldNotBe null
        found?.title shouldBe "Test title"
    }

    @Test
    fun `세션 조회`() {
        val id = sessionService.createSession(
                CreateSessionRequest(
                        title = "Test title",
                        description = "Test description",
                        message = "Test message",
                        generation = 22,
                        sessionTime = LocalDate.now(),
                        week = 3,
                        startAttendTime = LocalDateTime.now(),
                        endAttendTime = LocalDateTime.now()
                )
        )

        val found = sessionService.findSession(id)

        found?.title shouldBe "Test title"
    }

    @Test
    fun `특정 기수의 세션 조회`() {
        sessionService.createSession(
                CreateSessionRequest(
                        title = "Test title0",
                        description = "Test description",
                        message = "Test message",
                        generation = 22,
                        sessionTime = LocalDate.now(),
                        week = 2,
                        startAttendTime = LocalDateTime.now(),
                        endAttendTime = LocalDateTime.now()
                )
        )
        sessionService.createSession(
                CreateSessionRequest(
                        title = "Test title1",
                        description = "Test description",
                        message = "Test message",
                        generation = 22,
                        sessionTime = LocalDate.now(),
                        week = 3,
                        startAttendTime = LocalDateTime.now(),
                        endAttendTime = LocalDateTime.now()
                )
        )

        val founds = sessionService.findSessionByGeneration(22)

        founds.forEach {
            it.generation shouldBe 22
        }
    }

    @Test
    fun `세션 수정`() {
        val id = sessionService.createSession(
                CreateSessionRequest(
                        title = "Test title",
                        description = "Test description",
                        message = "Test message",
                        generation = 22,
                        sessionTime = LocalDate.now(),
                        week = 3,
                        startAttendTime = LocalDateTime.now(),
                        endAttendTime = LocalDateTime.now()
                )
        )

        sessionService.updateSession(id, UpdateSessionRequest(
                title = "Updated Title",
                description = "Test description",
                message = "Test message",
                generation = 22,
                sessionTime = LocalDate.now(),
                week = 3,
                startAttendTime = LocalDateTime.now(),
                endAttendTime = LocalDateTime.now()
        ))


        val found = sessionRepository.findByIdOrNull(id)

        found shouldNotBe null
        found?.title shouldBe "Updated Title"
    }

    @Test
    fun `세션 삭제`() {
        val id = sessionService.createSession(
                CreateSessionRequest(
                        title = "Test title",
                        description = "Test description",
                        message = "Test message",
                        generation = 22,
                        sessionTime = LocalDate.now(),
                        week = 3,
                        startAttendTime = LocalDateTime.now(),
                        endAttendTime = LocalDateTime.now()
                )
        )

        sessionService.deleteSession(id)

        val found = sessionRepository.findByIdOrNull(id)

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

        actual.title shouldBe "다가오는 세션"
    }

    @Test
    fun `세션이 존재하지 않으면 null 데이터를 반환한다`() {
        val member: Member = memberRepository.save(createNewMember())
        val generationMember: GenerationMember = createNewGenerationMember(memberId = member.id, generation = 22)
        generationMemberRepository.save(generationMember)

        val actual = sessionService.getSessionHome(member)

        actual.title shouldBe null
    }

    private fun generateSessions(): List<Session> {
        val today: LocalDate = LocalDate.now()
        val session1: Session = createNewSession(generation = 22, sessionTime = today.minusDays(3))
        val session2: Session = createNewSession(generation = 22, sessionTime = today.plusDays(10))
        val session3: Session = createNewSession(generation = 22, sessionTime = today.plusDays(3), title = "다가오는 세션")
        val session4: Session = createNewSession(generation = 23, sessionTime = today.plusDays(1))

        sessionRepository.save(session1)
        sessionRepository.save(session2)
        sessionRepository.save(session3)
        sessionRepository.save(session4)

        return listOf(session1, session2, session3, session4)
    }

    private fun generateAttendances(sessions: List<Session>, generationMember: GenerationMember) {
        for (session in sessions) {
            attendanceRepository.save(createNewAttendance(sessionId = session.id, generationMemberId = generationMember.id))
        }
    }

}
