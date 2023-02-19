package nexters.admin.domain.generation

import nexters.admin.exception.BadRequestException
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "generation")
class Generation(
        @Id
        var generation: Long = 0L,

        @Column
        var ceo: String? = null,

        @Enumerated(EnumType.STRING)
        @Column
        var status: GenerationStatus = GenerationStatus.BEFORE_ACTIVITY,
) {
}


