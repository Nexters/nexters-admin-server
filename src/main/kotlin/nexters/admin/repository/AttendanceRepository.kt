package nexters.admin.repository

import nexters.admin.domain.attendance.Attendance
import nexters.admin.domain.attendance.AttendanceStatus
import org.springframework.data.jpa.repository.JpaRepository

interface AttendanceRepository : JpaRepository<Attendance, Long> {
    fun findAllByGenerationMemberIdAndAttendanceStatusIn(generationMemberId: Long, statuses: List<AttendanceStatus>): List<Attendance>
}
