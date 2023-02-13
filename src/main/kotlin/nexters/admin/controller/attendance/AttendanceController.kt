package nexters.admin.controller.attendance

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import nexters.admin.domain.user.member.Member
import nexters.admin.service.attendance.AttendanceService
import nexters.admin.service.attendance.FindAttendanceProfileResponse
import nexters.admin.support.auth.LoggedInMember
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Attendance", description = "출석")
@RequestMapping("/api/attendance")
@RestController
class AttendanceController(
        private val attendanceService: AttendanceService
) {
    @Operation(summary = "내 출석 정보 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/me")
    fun findAttendanceProfile(@LoggedInMember member: Member): ResponseEntity<FindAttendanceProfileResponse> {
        val findAttendanceProfileResponse = attendanceService.getAttendanceProfile(member)
        return ResponseEntity.ok(findAttendanceProfileResponse)
    }
}
