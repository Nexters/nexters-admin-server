package nexters.admin.controller.user

import nexters.admin.common.auth.LoggedInMember
import nexters.admin.domain.user.member.Member
import nexters.admin.service.auth.AuthService
import nexters.admin.service.auth.LoginRequest
import nexters.admin.service.user.MemberService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

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
    @PostMapping("/password")
    fun changePassword(
            @LoggedInMember member: Member,
            @RequestBody @Valid request: UpdatePasswordRequest): ResponseEntity<Void> {
        memberService.updatePassword(member, request.password)
        return ResponseEntity.ok().build()
    }
}
