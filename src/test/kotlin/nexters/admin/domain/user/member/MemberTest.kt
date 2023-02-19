package nexters.admin.domain.user.member

import io.kotest.matchers.shouldBe
import nexters.admin.testsupport.PHONE_NUMBER
import nexters.admin.testsupport.createNewMember
import nexters.admin.domain.user.Password
import org.junit.jupiter.api.Test

class MemberTest {

    @Test
    fun `회원 정보 수정`() {
        val member = createNewMember()
        val prevStatus = member.status

        member.update(name = "김태현", gender = Gender.MALE, phoneNumber = PHONE_NUMBER)

        member.name shouldBe "김태현"
        member.status shouldBe prevStatus
    }

    @Test
    fun `회원 활동 정보 수정`() {
        val member = createNewMember()
        val prevName = member.name

        member.update(status = MemberStatus.CERTIFICATED)

        member.name shouldBe prevName
        member.status shouldBe MemberStatus.CERTIFICATED
    }

    @Test
    fun `비밀번호 일치 여부 반환`() {
        val password = "abcd1234"
        val member = createNewMember(password = password)

        member.isSamePassword(Password(password)) shouldBe true
    }

    @Test
    fun `비밀번호 수정시 초기화 여부도 수정`() {
        val member = createNewMember()
        member.updatePassword(Password("abcd1234!"))

        member.hasChangedPassword shouldBe true
    }
}
