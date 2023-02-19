package nexters.admin.domain.generation

import javax.persistence.*

@Entity
@Table(name = "generation")
class Generation(
        @Column(unique = true)
        var generation: Long = 0L,

        @Column
        var ceo: String? = null,

        @Enumerated(EnumType.STRING)
        @Column
        var status: GenerationStatus = GenerationStatus.BEFORE_ACTIVITY,
) {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = 0L
}
