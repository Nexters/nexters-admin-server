package nexters.admin.domain.user.administrator

import nexters.admin.domain.user.Password
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "administrator")
class Administrator(
        @Column(name = "username", nullable = false)
        val username: String,

        @AttributeOverride(name = "value", column = Column(name = "password", nullable = false))
        @Embedded
        var password: Password,

        @Column(name = "last_access_time")
        var lastAccessTime: LocalDateTime? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
}
