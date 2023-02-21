package nexters.admin.service.session

import nexters.admin.domain.attendance.Attendance
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.session.Session
import nexters.admin.domain.session.SessionStatus
import nexters.admin.domain.session.SessionStatus.*
import java.time.LocalDate
import java.time.LocalDateTime

data class CreateSessionRequest(
        val title: String,
        val description: String,
        val message: String,
        val generation: Int,
        val sessionTime: LocalDate,
        val week: Int,
        val startAttendTime: LocalDateTime,
        val endAttendTime: LocalDateTime
)

data class CreateSessionResponse(
        val sessionId: Long
)

data class UpdateSessionRequest(
        val title: String,
        val description: String,
        val message: String,
        val generation: Int,
        val sessionTime: LocalDate,
        val week: Int,
        val startAttendTime: LocalDateTime,
        val endAttendTime: LocalDateTime
)

data class FindSessionHomeResponse(
        val sessionDate: LocalDate?,
        val title: String?,
        val description: String?,
        val sessionStatus: SessionStatus?,
        val attendanceStatus: AttendanceStatus?,
        val attendanceTime: LocalDateTime?
) {
    companion object {
        fun of(): FindSessionHomeResponse {
            return FindSessionHomeResponse(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
            )
        }

        fun of(session: Session, attendance: Attendance): FindSessionHomeResponse {
            return FindSessionHomeResponse(
                    session.sessionTime,
                    session.title,
                    session.description,
                    findSessionStatus(session),
                    attendance.attendanceStatus,
                    attendance.attendTime
            )
        }

        private fun findSessionStatus(session: Session): SessionStatus {
            session.startAttendTime?.let {
                session.endAttendTime?.let {
                    return EXPIRED
                }
                return ONGOING
            }
            return PENDING
        }
    }

}
