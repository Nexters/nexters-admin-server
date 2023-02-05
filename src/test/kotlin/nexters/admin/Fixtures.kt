package nexters.admin

import nexters.admin.domain.user.Password
import nexters.admin.domain.user.member.Gender
import nexters.admin.domain.user.member.Member
import nexters.admin.domain.user.member.MemberStatus

fun createNewMember(
        username: String = "정진우",
        password: String = "1234",
        email: String = "jweong@gmail.com",
        gender: Gender = Gender.MALE,
        phoneNumber: String =  "01012345678",
        status: MemberStatus = MemberStatus.NOT_COMPLETION,
        hasChangedPassword: Boolean = false
): Member {
    return Member(username, Password(password), email, gender, phoneNumber, status, hasChangedPassword)
}
