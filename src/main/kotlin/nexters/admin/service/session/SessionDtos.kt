package nexters.admin.service.session

import com.fasterxml.jackson.annotation.JsonFormat
import nexters.admin.domain.attendance.Attendance
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.session.Session
import nexters.admin.domain.session.SessionStatus
import nexters.admin.domain.session.SessionStatus.EXPIRED
import nexters.admin.domain.session.SessionStatus.ONGOING
import nexters.admin.domain.session.SessionStatus.PENDING
import java.time.LocalDate
import java.time.LocalDateTime

data class CreateSessionResponse(
        val sessionId: Long,
)

data class FindSessionResponses(
        val data: List<FindSessionResponse>,
)

data class FindSessionResponse(
        val id: Long,
        val title: String?,
        val description: String?,
        val generation: Int,
        val sessionTime: LocalDate?,
        val week: Int,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        val startAttendTime: LocalDateTime?,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        val endAttendTime: LocalDateTime?,
) {
    companion object {
        fun from(session: Session): FindSessionResponse {
            return FindSessionResponse(
                    session.id,
                    session.title,
                    session.description,
                    session.generation,
                    session.sessionTime,
                    session.week,
                    session.startAttendTime,
                    session.endAttendTime
            )
        }
    }
}

data class FindSessionHomeResponse(
        val data: SessionHomeResponse?,
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
        val sessionDate: LocalDate,
        val title: String,
        val week: Int,
        val description: String?,
        val sessionStatus: SessionStatus,
        val attendanceStatus: AttendanceStatus,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        val attendanceTime: LocalDateTime?,
) {
    companion object {
        fun of(session: Session, attendance: Attendance): SessionHomeResponse {
            return SessionHomeResponse(
                    session.sessionTime,
                    session.title,
                    session.week,
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
