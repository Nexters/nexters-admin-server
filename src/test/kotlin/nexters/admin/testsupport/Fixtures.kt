package nexters.admin.testsupport

import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.generation_member.Position
import nexters.admin.domain.generation_member.SubPosition
import nexters.admin.domain.user.Password
import nexters.admin.domain.user.administrator.Administrator
import nexters.admin.domain.user.member.Gender
import nexters.admin.domain.user.member.Member
import nexters.admin.domain.user.member.MemberStatus
import java.time.LocalDateTime

const val ADMIN_USERNAME = "admin"
const val PHONE_NUMBER = "01012345678"

fun createNewMember(
        name: String = "정진우",
        password: String = "1234",
        email: String = "jweong@gmail.com",
        gender: Gender = Gender.MALE,
        phoneNumber: String = PHONE_NUMBER,
        status: MemberStatus = MemberStatus.NOT_COMPLETION,
        hasChangedPassword: Boolean = false,
): Member {
    return Member(name, Password(password), email, gender, phoneNumber, status, hasChangedPassword)
}

fun createNewGenerationMember(
        memberId: Long = 0L,
        generation: Int = 22,
        position: Position = Position.DEVELOPER,
        subPosition: SubPosition = SubPosition.BE,
        score: Int = 100,
        isCompletable: Boolean = true,
        isManager: Boolean = false,
): GenerationMember {
    return GenerationMember(memberId, generation, position, subPosition, score, isCompletable, isManager)
}

fun createNewAdmin(
        username: String = "root",
        password: String = "1234",
        lastAccessTime: LocalDateTime = LocalDateTime.now(),
): Administrator {
    return Administrator(username, Password(password), lastAccessTime)
}
