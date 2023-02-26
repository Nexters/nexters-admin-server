package nexters.admin.service.attendance

import com.fasterxml.jackson.annotation.JsonFormat
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
                    sessionToAttendance.map { AttendanceResponse.of(it.key, it.value) }
            )
        }
    }
}

data class AttendanceResponse(
        val title: String,
        val week: Int,
        val sessionDate: LocalDate?,
        val attendanceStatus: AttendanceStatus,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        val attendanceTime: LocalDateTime?,
        val penaltyScore: Int
) {
    companion object {
        fun of(session: Session, attendance: Attendance): AttendanceResponse {
            return AttendanceResponse(
                    session.title ?: "",
                    session.week,
                    session.sessionDate,
                    attendance.attendanceStatus,
                    attendance.attendTime,
                    attendance.attendanceStatus.penaltyScore
            )
        }
    }
}
