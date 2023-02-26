package nexters.admin.repository

import nexters.admin.domain.attendance.Attendance
import nexters.admin.domain.attendance.AttendanceStatus
import org.springframework.data.jpa.repository.JpaRepository

fun AttendanceRepository.findGenerationAttendancesIn(generationMemberId: Long, statuses: List<AttendanceStatus>): List<Attendance> {
    return findAllByGenerationMemberIdAndAttendanceStatusIn(generationMemberId, statuses)
}

fun AttendanceRepository.findAllPendingAttendanceOf(sessionId: Long): List<Attendance> {
    return findAllBySessionIdAndAttendanceStatus(sessionId, AttendanceStatus.PENDING)
}

interface AttendanceRepository : JpaRepository<Attendance, Long> {
    fun findAllByGenerationMemberId(generationMemberId: Long): List<Attendance>
    fun findAllByGenerationMemberIdAndAttendanceStatusIn(generationMemberId: Long, statuses: List<AttendanceStatus>): List<Attendance>
    fun findByGenerationMemberIdAndSessionId(generationMemberId: Long, sessionId: Long): Attendance?
    fun findAllBySessionIdAndAttendanceStatus(sessionId: Long, statuses: AttendanceStatus): List<Attendance>
    fun findBySessionIdAndGenerationMemberId(sessionId: Long, generationMemberId: Long): Attendance?
}
