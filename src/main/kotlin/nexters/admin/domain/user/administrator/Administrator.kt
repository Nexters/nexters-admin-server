package nexters.admin.domain.user.administrator

import nexters.admin.domain.user.Password
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "administrator")
class Administrator(
        @Column(name = "username", nullable = false)
        val username: String,

        @Column(name = "password", nullable = false)
        var password: Password,

        @Column(name = "last_access_time")
        var lastAccessTime: LocalDateTime? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun isSamePassword(password: Password): Boolean {
        return this.password.isSamePassword(password)
    }

    fun updateLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now()
    }
}
