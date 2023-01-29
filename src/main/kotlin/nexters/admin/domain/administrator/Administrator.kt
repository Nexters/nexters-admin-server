package nexters.admin.domain.administrator

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "administrator")
class Administrator(
        @Column(name = "username", nullable = false)
        val username: String,

        @Column(name = "password", nullable = false)
        var password: String,

        @Column(name = "last_access_time")
        var lastAccessTime: LocalDateTime? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
}
