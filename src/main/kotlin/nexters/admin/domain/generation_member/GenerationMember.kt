package nexters.admin.domain.generation_member

import nexters.admin.domain.user.member.Member
import javax.persistence.*

@Entity
@Table(name = "generation_member")
class GenerationMember(
        @Column(name = "generation", length = 20)
        val generation: Int,

        @Column(name = "position", nullable = false, length = 30)
        val position: String,

        @Column(name = "sub_position", nullable = false, length = 30)
        val subPosition: String,

        @Column(name = "score", nullable = false)
        var score: Int = 100,

        @Column(name = "is_completable", nullable = false)
        var isCompletable: Boolean = false,

        @Column(name = "is_manager", nullable = false)
        val isManager: Boolean = false
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    @ManyToOne
    @JoinColumn(name = "member_id")
    var member: Member? = null
}
