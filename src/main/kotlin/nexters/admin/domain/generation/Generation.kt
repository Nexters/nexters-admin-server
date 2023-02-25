package nexters.admin.domain.generation

import javax.persistence.*

@Entity
@Table(name = "generation")
class Generation(
        @Column(name = "generation", unique = true, nullable = false)
        var generation: Int,

        @Column(name = "ceo")
        var ceo: String? = null,

        @Enumerated(EnumType.STRING)
        @Column(name = "status")
        var status: GenerationStatus = GenerationStatus.BEFORE_ACTIVITY,
) {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = 0L
}
