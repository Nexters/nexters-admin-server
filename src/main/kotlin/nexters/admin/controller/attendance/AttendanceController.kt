package nexters.admin.controller.attendance

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import nexters.admin.domain.user.member.Member
import nexters.admin.service.attendance.AttendanceService
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
        qrCodeService.endAttendance()
        return ResponseEntity.ok().build()
    }
}
