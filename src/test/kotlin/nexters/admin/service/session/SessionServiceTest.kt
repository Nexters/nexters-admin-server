package nexters.admin.service.session

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nexters.admin.controller.session.CreateSessionRequest
import nexters.admin.controller.session.UpdateSessionRequest
import nexters.admin.repository.SessionRepository
import nexters.admin.testsupport.ApplicationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import java.time.LocalDateTime

@ApplicationTest
class SessionServiceTest(
        @Autowired private val sessionService: SessionService,
        @Autowired private val sessionRepository: SessionRepository,
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
}
