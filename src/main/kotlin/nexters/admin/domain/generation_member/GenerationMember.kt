package nexters.admin.domain.generation_member

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "generation_member")
class GenerationMember(
        @Column(name = "member_id")
        var memberId: Long? = null,

        @Column(name = "generation", length = 20)
        val generation: Int,

        @Column(name = "position", length = 30)
        val position: Position?,

        @Column(name = "sub_position", length = 30)
        val subPosition: SubPosition?,

        @Column(name = "score", nullable = false)
        var score: Int = 100,

        @Column(name = "is_completable", nullable = false)
        var isCompletable: Boolean = true,

        @Column(name = "is_manager", nullable = false)
        val isManager: Boolean = false,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
}
