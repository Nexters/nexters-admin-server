package nexters.admin.domain.user.administrator

import nexters.admin.domain.user.Password
import java.time.LocalDateTime
import javax.persistence.AttributeOverride
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "administrator")
class Administrator(
        @Column(name = "username", unique = true, nullable = false)
        val username: String,

        @Embedded
        var password: Password,

        @Column(name = "last_access_time")
        var lastAccessTime: LocalDateTime = LocalDateTime.now(),
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
