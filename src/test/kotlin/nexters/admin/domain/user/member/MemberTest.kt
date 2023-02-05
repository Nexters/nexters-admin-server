package nexters.admin.domain.user.member

import io.kotest.matchers.shouldBe
import nexters.admin.createNewMember
import nexters.admin.domain.user.Password
import org.junit.jupiter.api.Test

class MemberTest {

    @Test
    fun `비밀번호 일치 여부 반환`() {
        val password = "abcd1234"
        val member = createNewMember(password = password)

        member.isSamePassword(Password(password)) shouldBe true
    }

    @Test
    fun `비밀번호 수정시 초기화 여부도 수정`() {
        val member = createNewMember()
        val newPassword = Password("abcd1234!")

        member.updatePassword(newPassword)

        member.hasChangedPassword shouldBe true
    }
}
