package nexters.admin.domain.member

import javax.persistence.*

@Entity
@Table(name = "member")
class Member(
        @Column(name = "username", nullable = false)
        val username: String,

        @Column(name = "password", nullable = false)
        var password: String,

        @Column(name = "gender", nullable = false, length = 10)
        val gender: Gender,

        @Column(name = "phone_number", nullable = false, length = 15)
        var phoneNumber: String,

        @Column(name = "status", nullable = false, length = 15)
        var status: MemberStatus = MemberStatus.NOT_COMPLETION,

        @Column(name = "is_init_password", nullable = false)
        var isInitPassword: Boolean = false
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
}
