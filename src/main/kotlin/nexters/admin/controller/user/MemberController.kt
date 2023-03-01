package nexters.admin.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import nexters.admin.controller.auth.LoggedInMemberRequest
import nexters.admin.service.user.FindAllMembersResponse
import nexters.admin.service.user.FindProfileResponse
import nexters.admin.service.user.MemberService
import nexters.admin.support.auth.LoggedInMember
import nexters.admin.support.utils.parseCsvFileToMap
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.validation.Valid

@Tag(name = "Members", description = "유저")
@RequestMapping("/api/members")
@RestController
class MemberController(
        private val memberService: MemberService,
) {
    @Operation(summary = "[관리자 페이지] 회원 단건 생성")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    fun createMemberByAdministrator(@RequestBody @Valid request: CreateMemberRequest): ResponseEntity<Void> {
        memberService.createMemberByAdministrator(request)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "[관리자 페이지] csv 파일 기반 회원 복수 생성")
    @SecurityRequirement(name = "JWT")
    @PostMapping(value = ["/bulk"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createMembersByAdministrator(
            @RequestParam generation: Int,
            @RequestParam csvFile: MultipartFile,
    ): ResponseEntity<Void> {
        memberService.createGenerationMembers(generation, parseCsvFileToMap(csvFile))
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "[관리자 페이지] 회원 전체 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    fun findAllByAdministrator(): ResponseEntity<FindAllMembersResponse> {
        val findAllMembersResponse = memberService.findAllByAdministrator()
        return ResponseEntity.ok(findAllMembersResponse)
    }

    @Operation(summary = "[관리자 페이지] 회원 정보 수정 및 기수회원 동기화")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{id}")
    fun update(
            @PathVariable id: Long,
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
            @RequestBody @Valid request: UpdateMemberPositionRequest,
    ): ResponseEntity<Void> {
        memberService.updatePositionByAdministrator(id, request.position, request.subPosition)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "비밀번호 업데이트")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/password")
    fun updatePassword(
            @LoggedInMember member: LoggedInMemberRequest,
            @RequestBody @Valid request: UpdatePasswordRequest,
    ): ResponseEntity<Void> {
        memberService.updatePassword(member, request.password)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "내 정보 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/me")
    fun findProfile(@LoggedInMember member: LoggedInMemberRequest): ResponseEntity<FindProfileResponse> {
        val findProfileResponse = memberService.getProfile(member)
        return ResponseEntity.ok(findProfileResponse)
    }

    @Operation(summary = "[관리자 페이지] 회원 삭제")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        memberService.deleteByAdministrator(id)
        return ResponseEntity.ok().build()
    }
}
