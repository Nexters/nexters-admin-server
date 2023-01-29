package nexters.admin.domain.attendance

import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.member.Member
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "attendance")
class Attendance(
        @Column(name = "attend_time")
        var attendTime: LocalDateTime? = null,

        @ManyToOne
        @JoinColumn(name = "generation_member_id")
        val generationMember: GenerationMember,

        @Column(name = "attendance_status")
        val attendanceStatus: AttendanceStatus
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    @ManyToOne
    @JoinColumn(name = "member_id")
    val member: Member? = null
}
