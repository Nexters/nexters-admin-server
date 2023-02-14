package nexters.admin.domain.user.member

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nexters.admin.exception.BadRequestException
import org.junit.jupiter.api.Test

class MemberStatusTest {

    @Test
    fun `올바른 활동 구분을 반환`() {
        val actual = MemberStatus.from("수료")

        actual shouldBe MemberStatus.CERTIFICATED
    }

    @Test
    fun `올바르지 않은 활동구분 값이 주어지면 예외를 반환`() {
        shouldThrow<BadRequestException> {
            MemberStatus.from("invalid")
        }
    }
}
