package nexters.admin.controller.attendance

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import nexters.admin.domain.user.member.Member
import nexters.admin.service.attendance.AttendanceActivityHistoryResponses
import nexters.admin.service.attendance.AttendanceActivityResponses
import nexters.admin.service.attendance.AttendanceService
import nexters.admin.service.attendance.AttendanceSessionResponses
import nexters.admin.service.attendance.FindAttendanceProfileResponse
import nexters.admin.service.attendance.QrCodeService
import nexters.admin.support.auth.LoggedInMember
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Tag(name = "Attendance", description = "출석")
@RequestMapping("/api/attendance")
@RestController
class AttendanceController(
        private val attendanceService: AttendanceService,
        private val qrCodeService: QrCodeService,
) {
    @Operation(summary = "출석 체크")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    fun attend(
            @LoggedInMember member: Member,
            @RequestBody @Valid request: ValidateQrCodeRequest
    ): ResponseEntity<Void> {
        attendanceService.attendWithQrCode(member, request.nonce)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "[관리자 페이지] 출석 가산점/감점 부여")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/{id}/additional-score")
    fun addExtraAttendanceScore(
            @PathVariable id: Long,
            @RequestBody @Valid request: ExtraAttendanceScoreChangeRequest
    ): ResponseEntity<Void> {
        attendanceService.addExtraAttendanceScoreByAdministrator(id, request.extraScoreChange, request.extraScoreNote)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "[관리자 페이지] 개인의 출석 상태 수정")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{id}/status")
    fun updateAttendanceStatus(
            @PathVariable id: Long,
            @RequestBody @Valid request: UpdateAttendanceStatusRequest
    ): ResponseEntity<Void> {
        attendanceService.updateAttendanceStatusByAdministrator(id, request.attendanceStatus, request.note)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "내 출석 정보 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/me")
    fun findAttendanceProfile(@LoggedInMember member: Member): ResponseEntity<FindAttendanceProfileResponse> {
        val findAttendanceProfileResponse = attendanceService.getAttendanceProfile(member)
        return ResponseEntity.ok(findAttendanceProfileResponse)
    }

    @Operation(summary = "[관리자 페이지] 현재 유효한 QR 코드 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/qr")
    fun getCurrentQrCode(): ResponseEntity<CurrentQrCodeResponse> {
        val qrCode = qrCodeService.getCurrentQrCode()
        return ResponseEntity.ok(CurrentQrCodeResponse.from((qrCode)))
    }

    @Operation(summary = "[관리자 페이지] 출석 시작 - QR 코드 자동 생성 시작")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/qr")
    fun startAttendance(@RequestBody @Valid request: InitializeQrCodesRequest): ResponseEntity<Void> {
        qrCodeService.initializeCodes(request.sessionId, request.qrCodeType)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "[관리자 페이지] 출석 종료 - QR 코드 생성 중단 및 자동 무단 결석 처리")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/qr")
    fun endAttendance(): ResponseEntity<Void> {
        attendanceService.endAttendance()
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "[관리자 페이지] 해당 세션에 대한 출석 정보 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{sessionId}")
    fun findAllBySessionId(@PathVariable sessionId: Long): ResponseEntity<AttendanceSessionResponses> {
        val attendanceSessionResponses = attendanceService.findAttendancesBySessionId(sessionId)
        return ResponseEntity.ok(attendanceSessionResponses)
    }

    @Operation(summary = "[관리자 페이지] 활동 관리 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/activity")
    fun findAllActivities(@PathVariable generation: Int): ResponseEntity<AttendanceActivityResponses> {
        val attendanceActivityResponses = attendanceService.findAllActivities(generation)
        return ResponseEntity.ok(attendanceActivityResponses)
    }

    @Operation(summary = "[관리자 페이지] 활동 관리 자세히 보기")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/activity/{generationMemberId}")
    fun findActivityHistory(
            @PathVariable generationMemberId: Long,
            @PathVariable generation: Int,
    ): ResponseEntity<AttendanceActivityHistoryResponses> {
        val attendanceActivityHistoryResponses = attendanceService.findActivityHistory(generationMemberId, generation)
        return ResponseEntity.ok(attendanceActivityHistoryResponses)
    }
}
