package nexters.admin.domain.user.member

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nexters.admin.common.exception.UnauthenticatedException
import nexters.admin.domain.user.Password
import org.junit.jupiter.api.Test

class MemberTest {

    @Test
    fun `비밀번호 검증`() {
        val member = Member("정진우", Password("1234"), "jweong@gmail.com", Gender.MALE, "01012345678", MemberStatus.NOT_COMPLETION, true)

        shouldThrow<UnauthenticatedException> {
            member.validatePassword(Password("abcd1234!"))
        }
    }

    @Test
    fun `비밀번호 수정`() {
        val member = Member("정진우", Password("1234"), "jweong@gmail.com", Gender.MALE, "01012345678", MemberStatus.NOT_COMPLETION, true)
        val newPassword = Password("abcd1234!")

        member.updatePassword(newPassword)

        member.password.value shouldBe newPassword.value
        member.isInitPassword shouldBe false
    }
}