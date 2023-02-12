package nexters.admin.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import nexters.admin.domain.user.administrator.Administrator
import nexters.admin.domain.user.member.Member
import nexters.admin.service.auth.AuthService
import nexters.admin.service.auth.LoginRequest
import nexters.admin.service.user.FindAllMembersResponse
import nexters.admin.service.user.FindProfileResponse
import nexters.admin.service.user.MemberService
import nexters.admin.support.auth.LoggedInAdmin
import nexters.admin.support.auth.LoggedInMember
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Tag(name = "Members", description = "유저")
@RequestMapping("/api/members")
@RestController
class MemberController(
        private val authService: AuthService,
        private val memberService: MemberService,
) {
    @Operation(summary = "[관리자 페이지] 회원 단건 생성")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    fun createMemberByAdministrator(
            @LoggedInAdmin administrator: Administrator,
            @RequestBody @Valid request: CreateMemberRequest,
    ): ResponseEntity<Void> {
        memberService.createMemberByAdministrator(request)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "[관리자 페이지] 회원 전체 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    fun findAllByAdministrator(@LoggedInAdmin administrator: Administrator): ResponseEntity<FindAllMembersResponse> {
        val findAllMembersResponse = memberService.findAllByAdministrator()
        return ResponseEntity.ok(findAllMembersResponse)
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginRequest): ResponseEntity<TokenResponse> {
        val token = authService.generateMemberToken(request)
        return ResponseEntity.ok(TokenResponse(token))
    }

    @Operation(summary = "[관리자 페이지] 회원 정보수정")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{id}")
    fun update(
            @PathVariable id: Long,
            @LoggedInAdmin administrator: Administrator,
            @RequestBody @Valid request: UpdateMemberRequest,
    ): ResponseEntity<Void> {
        memberService.updateMemberByAdministrator(id, request)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "[관리자 페이지] 회원 활동구분 수정")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{id}/status")
    fun updateStatus(
            @PathVariable id: Long,
            @LoggedInAdmin administrator: Administrator,
            @RequestBody @Valid request: UpdateMemberStatusRequest,
    ): ResponseEntity<Void> {
        memberService.updateStatusByAdministrator(id, request.status)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "[관리자 페이지] 회원 직군 수정")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{id}/position")
    fun updatePosition(
            @PathVariable id: Long,
            @LoggedInAdmin administrator: Administrator,
            @RequestBody @Valid request: UpdateMemberPositionRequest,
    ): ResponseEntity<Void> {
        memberService.updatePositionByAdministrator(id, request.position, request.subPosition)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "비밀번호 업데이트")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/password")
    fun updatePassword(
            @LoggedInMember member: Member,
            @RequestBody @Valid request: UpdatePasswordRequest,
    ): ResponseEntity<Void> {
        memberService.updatePassword(member, request.password)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "내 정보 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/me")
    fun findProfile(@LoggedInMember member: Member): ResponseEntity<FindProfileResponse> {
        val findProfileResponse = memberService.getProfile(member)
        return ResponseEntity.ok(findProfileResponse)
    }

    @Operation(summary = "[관리자 페이지] 회원 삭제")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{id}")
    fun delete(
            @PathVariable id: Long,
            @LoggedInAdmin administrator: Administrator,
    ): ResponseEntity<Void> {
        memberService.deleteByAdministrator(id)
        return ResponseEntity.ok().build()
    }
}
