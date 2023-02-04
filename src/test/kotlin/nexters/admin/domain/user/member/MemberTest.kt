package nexters.admin.domain.user.member

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nexters.admin.createNewMember
import nexters.admin.support.exception.UnauthenticatedException
import nexters.admin.domain.user.Password
import org.junit.jupiter.api.Test

class MemberTest {

    @Test
    fun `잘못된 비밀번호 입력시 예외`() {
        val member = createNewMember()

        shouldThrow<UnauthenticatedException> {
            member.checkSamePassword(Password("abcd1234!"))
        }
    }

    @Test
    fun `비밀번호 수정시 초기화 여부도 수정`() {
        val member = createNewMember()
        val newPassword = Password("abcd1234!")

        member.updatePassword(newPassword)

        member.hasChangedPassword shouldBe true
    }
}
