package nexters.admin.service.attendance

import com.fasterxml.jackson.annotation.JsonProperty
import nexters.admin.domain.attendance.Attendance
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.session.Session
import java.time.LocalDate
import java.time.LocalDateTime

data class FindAttendanceProfileResponse(
        @get:JsonProperty(value = "isGenerationMember")
        val isGenerationMember: Boolean,
        val attendanceData: AttendanceProfileResponse?
) {
    companion object {
        fun of(): FindAttendanceProfileResponse {
            return FindAttendanceProfileResponse(false, null)
        }

        fun of(generationMember: GenerationMember, sessionToAttendance: Map<Session, Attendance>): FindAttendanceProfileResponse {
            return FindAttendanceProfileResponse(true, AttendanceProfileResponse.of(generationMember, sessionToAttendance))
        }
    }
}

data class AttendanceProfileResponse(
        val score: Int,
        @get:JsonProperty(value = "isCompletable")
        val isCompletable: Boolean,
        val attendances: List<AttendanceResponse>
) {
    companion object {
        fun of(generationMember: GenerationMember, sessionToAttendance: Map<Session, Attendance>): AttendanceProfileResponse {
            return AttendanceProfileResponse(
                    generationMember.score ?: 100,
                    generationMember.isCompletable,
                    sessionToAttendance.map { (session, attendance) ->
                        AttendanceResponse.of(session, attendance)
                    }
            )
        }
    }
}

data class AttendanceResponse(
        val title: String,
        val week: Int,
        val sessionDate: LocalDate?,
        val attendanceStatus: AttendanceStatus,
        val attendanceTime: LocalDateTime?,
        val score: Int
) {
    companion object {
        fun of(session: Session, attendance: Attendance): AttendanceResponse {
            return AttendanceResponse(
                    session.title ?: "",
                    session.week,
                    session.sessionTime,
                    attendance.attendanceStatus,
                    attendance.attendTime,
                    attendance.attendanceStatus.score
            )
        }
    }
}
