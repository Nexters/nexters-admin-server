package nexters.admin.domain.session

import nexters.admin.domain.generation_member.GenerationMember
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "session")
class Session(
        @Column(name = "description")
        var description: String? = null,

        @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], fetch = FetchType.LAZY)
        @JoinColumn(name = "judgment_id", nullable = false)
        var generations: MutableList<GenerationMember>, // TODO: 기수는 여러 개 가능 ex. 17, 21 -> 우선 기수를 일대다로 갖고 있게 함

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
