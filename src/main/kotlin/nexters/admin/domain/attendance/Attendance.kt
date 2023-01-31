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

        @Enumerated(EnumType.STRING)
        @Column(name = "attendance_status")
        val attendanceStatus: AttendanceStatus
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
}
