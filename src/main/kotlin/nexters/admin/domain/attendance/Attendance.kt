package nexters.admin.domain.attendance

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "attendance")
class Attendance(
        @Column(name = "attend_time")
        var attendTime: LocalDateTime? = null,

        @Column(name = "generation_member_id", nullable = false)
        val generationMemberId: Long,

        @Column(name = "session_id", nullable = false)
        val sessionId: Long,

        @Enumerated(EnumType.STRING)
        @Column(name = "attendance_status", nullable = false)
        var attendanceStatus: AttendanceStatus,

        @Column(name = "extra_score_note")
        var extraScoreNote: String? = "",

        @Column(name = "additional_point", nullable = false)
        var scoreChanged: Int = 0,

        @Column(name = "note")
        var note: String? = ""
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun updateStatus(attendanceStatus: AttendanceStatus) {
        this.attendTime = LocalDateTime.now()
        this.attendanceStatus = attendanceStatus
    }
}
