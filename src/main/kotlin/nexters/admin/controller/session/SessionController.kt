package nexters.admin.controller.session

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import nexters.admin.domain.session.Session
import nexters.admin.service.session.CreateSessionResponse
import nexters.admin.service.session.SessionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Tag(name = "Session", description = "세션")
@RequestMapping("/api/sessions")
@RestController
class SessionController(
        private val sessionService: SessionService,
) {

    @Operation(summary = "메인 세션 조회")
    @GetMapping("/home")
    fun getSessionHome() {
        // Todo
        // jwt에서 유저아이디 가져온다음 유저에 따라 다른 response 를 줘야함
        // ResponseBody {
        //    sessionDate: LocalDate
        //    title: string
        //    description: string
        //		sessionStatus: string // PENDING=출석 체크 시작 전, ONGOING=출석 중, EXPIRED=출석 종료
        //    attendanceStatus: string // PENDING, TARDY, ATTENDED, UNAUTHORIZED_ABSENCE, AUTHORIZED_ABSENCE
        //    attendanceTime: LocalDateTime
        // }
    }

    @Operation(summary = "[관리자 페이지] 특정 기수의 세션 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    fun findSessionByGeneration(@RequestParam generation: Int): ResponseEntity<List<Session>> {
        val sessions = sessionService.findSessionByGeneration(generation)
        return ResponseEntity.ok(sessions)
    }

    @Operation(summary = "[관리자 페이지] 세션 생성")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    fun createSession(
            @RequestBody @Valid request: CreateSessionRequest
    ): ResponseEntity<CreateSessionResponse> {
        val sessionId = sessionService.createSession(request)
        return ResponseEntity.ok(CreateSessionResponse(sessionId))
    }

    @SecurityRequirement(name = "JWT")
    @GetMapping("/{id}")
    fun findSessionById(
            @PathVariable id: Long
    ): ResponseEntity<Session> {
        val session = sessionService.findSession(id)
        return ResponseEntity.ok(session)
    }

    @Operation(summary = "[관리자 페이지] 세션 수정")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{id}")
    fun updateSession(
            @PathVariable id: Long,
            @RequestBody @Valid request: UpdateSessionRequest
    ) {
        sessionService.updateSession(id, request)
    }

    @Operation(summary = "[관리자 페이지] 세션 삭제")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{id}")
    fun deleteSession(
            @PathVariable id: Long,
    ) {
        sessionService.deleteSession(id)
    }

}

