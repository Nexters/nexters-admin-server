package nexters.admin.controller.session

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import nexters.admin.controller.auth.LoggedInMemberRequest
import nexters.admin.domain.user.member.Member
import nexters.admin.service.session.CreateSessionResponse
import nexters.admin.service.session.FindSessionHomeResponse
import nexters.admin.service.session.FindSessionResponse
import nexters.admin.service.session.FindSessionResponses
import nexters.admin.service.session.SessionService
import nexters.admin.support.auth.LoggedInMember
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
    @SecurityRequirement(name = "JWT")
    @GetMapping("/home")
    fun getSessionHome(@LoggedInMember member: LoggedInMemberRequest): ResponseEntity<FindSessionHomeResponse> {
        val findSessionHomeResponse = sessionService.getSessionHome(member)
        return ResponseEntity.ok(findSessionHomeResponse)
    }

    @Operation(summary = "[관리자 페이지] 특정 기수의 세션 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    fun findSessionByGeneration(@RequestParam generation: Int): ResponseEntity<FindSessionResponses> {
        val findSessionResponses = sessionService.findSessionByGeneration(generation)
        return ResponseEntity.ok(findSessionResponses)
    }

    @Operation(summary = "[관리자 페이지] 세션 생성")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    fun createSession(
            @RequestBody @Valid request: CreateSessionRequest,
    ): ResponseEntity<CreateSessionResponse> {
        val sessionId = sessionService.createSession(request)
        return ResponseEntity.ok(CreateSessionResponse(sessionId))
    }

    @SecurityRequirement(name = "JWT")
    @GetMapping("/{id}")
    fun findSessionById(
            @PathVariable id: Long,
    ): ResponseEntity<FindSessionResponse> {
        val findSessionResponse = sessionService.findSession(id)
        return ResponseEntity.ok(findSessionResponse)
    }

    @Operation(summary = "[관리자 페이지] 세션 수정")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{id}")
    fun updateSession(
            @PathVariable id: Long,
            @RequestBody @Valid request: UpdateSessionRequest,
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
