package nexters.admin.domain.session

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "session")
class Session(
        @Column(name = "title")
        var title: String? = null,

        @Column(name = "description")
        var description: String? = null,

        @Column(name = "message")
        var message: String? = null,

        @Column(name = "generation", nullable = false)
        var generation: Int,

        @Column(name = "session_time")
        var sessionTime: LocalDate? = null,

        @Column(name = "week", nullable = false)
        var week: Int,

        @Column(name = "start_attend_time")
        var startAttendTime: LocalDateTime? = null,

        @Column(name = "end_attend_time")
        var endAttendTime: LocalDateTime? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
}
