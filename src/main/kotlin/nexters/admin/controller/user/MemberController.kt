package nexters.admin.controller.user

import nexters.admin.domain.user.administrator.Administrator
import nexters.admin.domain.user.member.Member
import nexters.admin.service.auth.AuthService
import nexters.admin.service.auth.LoginRequest
import nexters.admin.service.user.FindAllMembersResponse
import nexters.admin.service.user.MemberService
import nexters.admin.support.auth.LoggedInAdmin
import nexters.admin.support.auth.LoggedInMember
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RequestMapping("/api/members")
@RestController
class MemberController(
        private val authService: AuthService,
        private val memberService: MemberService,
) {
    @GetMapping
    fun findAllByAdministrator(@LoggedInAdmin administrator: Administrator): ResponseEntity<FindAllMembersResponse> {
        val findAllMembersResponse = memberService.findAllByAdministrator()
        return ResponseEntity.ok(findAllMembersResponse)
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginRequest): ResponseEntity<TokenResponse> {
        val token = authService.generateMemberToken(request)
        return ResponseEntity.ok(TokenResponse(token))
    }

    @PutMapping("/{id}")
    fun update(
            @LoggedInAdmin administrator: Administrator,
            @RequestBody @Valid request: UpdateMemberRequest,
    ): ResponseEntity<Void> {
        memberService.updateMemberByAdministrator(request)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/password")
    fun updatePassword(
            @LoggedInMember member: Member,
            @RequestBody @Valid request: UpdatePasswordRequest,
    ): ResponseEntity<Void> {
        memberService.updatePassword(member, request.password)
        return ResponseEntity.ok().build()
    }
}
