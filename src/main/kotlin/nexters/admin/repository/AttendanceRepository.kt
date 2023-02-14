package nexters.admin.repository

import nexters.admin.domain.attendance.Attendance
import nexters.admin.domain.attendance.AttendanceStatus
import org.springframework.data.jpa.repository.JpaRepository

fun AttendanceRepository.findGenerationAttendancesIn(generationMemberId: Long, statuses: List<AttendanceStatus>): List<Attendance> {
    return findAllByGenerationMemberIdAndAttendanceStatusIn(generationMemberId, statuses)
}

interface AttendanceRepository : JpaRepository<Attendance, Long> {
    fun findAllByGenerationMemberIdAndAttendanceStatusIn(generationMemberId: Long, statuses: List<AttendanceStatus>): List<Attendance>
}
