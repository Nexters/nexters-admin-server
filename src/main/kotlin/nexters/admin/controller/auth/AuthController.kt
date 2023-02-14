package nexters.admin.controller.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import nexters.admin.service.auth.AdminLoginRequest
import nexters.admin.service.auth.AuthService
import nexters.admin.service.auth.MemberLoginRequest
import nexters.admin.service.auth.MemberLoginResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Tag(name = "Auth", description = "인증")
@RequestMapping("/api/auth")
@RestController
class AuthController(
        private val authService: AuthService,
) {
    @Operation(summary = "관리자 로그인")
    @PostMapping("/login/admin")
    fun loginAdmin(@RequestBody @Valid request: AdminLoginRequest): ResponseEntity<TokenResponse> {
        val token = authService.generateAdminToken(request)
        return ResponseEntity.ok(TokenResponse(token))
    }

    @Operation(summary = "일반 회원 로그인")
    @PostMapping("/login/member")
    fun loginMember(@RequestBody @Valid request: MemberLoginRequest): ResponseEntity<MemberLoginResponse> {
        val token = authService.generateMemberToken(request)
        return ResponseEntity.ok(token)
    }
}
