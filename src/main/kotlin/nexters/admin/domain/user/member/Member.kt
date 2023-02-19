package nexters.admin.domain.user.member

import nexters.admin.domain.user.Password
import javax.persistence.*

private const val DEFAULT_INITIAL_PASSWORD_LENGTH = 4

@Entity
@Table(name = "member")
class Member(
        @Column(name = "name", nullable = false)
        var name: String,

        @Embedded
        var password: Password,

        @Column(name = "email", nullable = false)
        var email: String,

        @Enumerated(EnumType.STRING)
        @Column(name = "gender", nullable = false, length = 10)
        var gender: Gender,

        @Column(name = "phone_number", nullable = false, length = 15)
        var phoneNumber: String,

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 15)
        var status: MemberStatus = MemberStatus.NOT_COMPLETION,

        @Column(name = "has_changed_password", nullable = false)
        var hasChangedPassword: Boolean = false,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    companion object {
        fun of(
                name: String,
                email: String,
                gender: String,
                phoneNumber: String,
                status: String,
        ): Member {
            return Member(
                    name = name,
                    password = generateDefaultPassword(phoneNumber),
                    email = email,
                    gender = Gender.from(gender),
                    phoneNumber = phoneNumber,
                    status = MemberStatus.from(status)
            )
        }

        private fun generateDefaultPassword(phoneNumber: String): Password {
            return Password(phoneNumber.substring(phoneNumber.length - DEFAULT_INITIAL_PASSWORD_LENGTH, phoneNumber.length))
        }
    }

    fun update(
            name: String,
            gender: Gender,
            phoneNumber: String,
    ) {
        this.name = name
        this.gender = gender
        this.phoneNumber = phoneNumber
    }

    fun updateStatus(status: MemberStatus) {
        this.status = status
    }

    fun isSamePassword(password: Password): Boolean {
        return this.password.isSamePassword(password)
    }

    fun updatePassword(password: Password) {
        this.password = password
        this.hasChangedPassword = true
    }
}
