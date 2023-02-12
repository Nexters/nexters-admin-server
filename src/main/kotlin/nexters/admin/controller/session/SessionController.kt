package nexters.admin.controller.session

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import nexters.admin.domain.session.Session
import nexters.admin.domain.user.administrator.Administrator
import nexters.admin.service.session.CreateSessionRequest
import nexters.admin.service.session.CreateSessionResponse
import nexters.admin.service.session.SessionService
import nexters.admin.service.session.UpdateSessionRequest
import nexters.admin.support.auth.LoggedInAdmin
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Tag(name = "Session", description = "세션")
@RequestMapping("/api/sessinos")
@RestController
class SessionController(
        private val sessionService: SessionService,
) {

    @Operation(summary = "메인 세션 조회")
    @GetMapping("/home")
    fun getSessionHome() {

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

