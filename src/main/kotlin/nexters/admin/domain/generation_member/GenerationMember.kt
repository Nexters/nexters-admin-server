package nexters.admin.domain.generation_member

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

const val MAX_SCORE = 100

@Entity
@Table(name = "generation_member")
class GenerationMember(
        @Column(name = "member_id")
        var memberId: Long = 0L,

        @Column(name = "generation", length = 20)
        val generation: Int,

        @Enumerated(EnumType.STRING)
        @Column(name = "position", length = 30)
        var position: Position?,

        @Enumerated(EnumType.STRING)
        @Column(name = "sub_position", length = 30)
        var subPosition: SubPosition?,

        @Column(name = "score")
        var score: Int? = MAX_SCORE,

        @Column(name = "is_completable", nullable = false)
        var isCompletable: Boolean = true,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun updatePosition(position: Position?, subPosition: SubPosition?) {
        this.position = position
        this.subPosition = subPosition
    }

    fun updateScoreByChanges(changedScores: List<Int>) {
        this.score = changedScores.fold(MAX_SCORE) { totalScore, scoreChange -> totalScore + scoreChange }
    }

    fun isManager(): Boolean {
        return this.position == Position.MANAGER
    }
}
