package nexters.admin.domain.attendance

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "attendance")
class Attendance(
        @Column(name = "attend_time")
        var attendTime: LocalDateTime? = null,

        @Column(name = "generation_member_id")
        val generationMemberId: Long,

        @Column(name = "session_id")
        val sessionId: Long,

        @Enumerated(EnumType.STRING)
        @Column(name = "attendance_status")
        var attendanceStatus: AttendanceStatus
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun updateStatus(attendanceStatus: AttendanceStatus) {
        this.attendTime = LocalDateTime.now()
        this.attendanceStatus = attendanceStatus
    }
}
