package nexters.admin.service.session

import com.fasterxml.jackson.annotation.JsonFormat
import nexters.admin.domain.attendance.Attendance
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.session.Session
import nexters.admin.domain.session.SessionStatus
import nexters.admin.domain.session.SessionStatus.*
import java.time.LocalDate
import java.time.LocalDateTime

data class CreateSessionResponse(
        val sessionId: Long
)

data class FindSessionHomeResponse(
        val data: SessionHomeResponse?
) {
    companion object {
        fun of(): FindSessionHomeResponse {
            return FindSessionHomeResponse(null)
        }

        fun of(session: Session, attendance: Attendance): FindSessionHomeResponse {
            return FindSessionHomeResponse(SessionHomeResponse.of(session, attendance))
        }
    }
}

data class SessionHomeResponse(
        val sessionDate: LocalDate?,
        val title: String?,
        val description: String?,
        val sessionStatus: SessionStatus?,
        val attendanceStatus: AttendanceStatus?,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        val attendanceTime: LocalDateTime?
) {
    companion object {
        fun of(session: Session, attendance: Attendance): SessionHomeResponse {
            return SessionHomeResponse(
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
