package nexters.admin.controller.user

import nexters.admin.service.auth.AuthService
import nexters.admin.service.auth.LoginRequest
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
}
