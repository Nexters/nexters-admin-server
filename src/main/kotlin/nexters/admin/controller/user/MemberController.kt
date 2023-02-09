package nexters.admin.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import nexters.admin.support.auth.LoggedInMember
import nexters.admin.domain.user.member.Member
import nexters.admin.service.auth.AuthService
import nexters.admin.service.auth.LoginRequest
import nexters.admin.service.user.MemberService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Tag(name = "Users", description = "유저")
@RequestMapping("/api/users")
@RestController
class MemberController(
        private val authService: AuthService,
        private val memberService: MemberService,
) {
    // TODO: erase all API JavaDoc
    /**
     * POST /api/users/login
     * RequestBody {
     *      email: string,
     *      password: string
     * }
     * ResponseBody {
     *      data: string // valid JWT token
     * }
     */
    @Operation(summary = "로그인", description = "")
    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginRequest): ResponseEntity<TokenResponse> {
        val token = authService.generateMemberToken(request)
        return ResponseEntity.ok(TokenResponse(token))
    }

    /**
     * PUT /api/users/password
     * Authorization: Bearer valid.jwt.token
     * RequestBody {
     *      password: string
     * }
     */
    @Operation(summary = "비밀번호 업데이트", description = "")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/password")
    fun updatePassword(
            @LoggedInMember member: Member,
            @RequestBody @Valid request: UpdatePasswordRequest,
    ): ResponseEntity<Void> {
        memberService.updatePassword(member, request.password)
        return ResponseEntity.ok().build()
    }
}
