package nexters.admin.domain.user.member

import nexters.admin.domain.user.Password
import javax.persistence.*

@Entity
@Table(name = "member")
class Member(
        @Column(name = "username", nullable = false)
        val username: String,

        @Column(name = "password", nullable = false)
        var password: Password,

        @Column(name = "email", nullable = false)
        var email: String,

        @Enumerated(EnumType.STRING)
        @Column(name = "gender", nullable = false, length = 10)
        val gender: Gender,

        @Column(name = "phone_number", nullable = false, length = 15)
        var phoneNumber: String,

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 15)
        var status: MemberStatus = MemberStatus.NOT_COMPLETION,

        @Column(name = "has_changed_password", nullable = false)
        var hasChangedPassword: Boolean = false
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun isSamePassword(password: Password): Boolean {
       return this.password.value == password.value
    }

    fun updatePassword(password: Password) {
        this.password = password
        this.hasChangedPassword = true
    }
}
