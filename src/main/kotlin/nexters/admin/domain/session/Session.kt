package nexters.admin.domain.session

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "session")
class Session(
        @Column(name = "title", nullable = false)
        var title: String,

        @Column(name = "description")
        var description: String? = null,

        @Column(name = "generation", nullable = false)
        var generation: Int,

        @Column(name = "session_time", nullable = false)
        var sessionTime: LocalDate,

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
