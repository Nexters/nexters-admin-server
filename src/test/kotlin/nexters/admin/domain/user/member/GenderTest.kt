package nexters.admin.domain.user.member

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nexters.admin.domain.generation_member.SubPosition
import nexters.admin.exception.BadRequestException
import org.junit.jupiter.api.Test

class GenderTest {

    @Test
    fun `올바른 성별 반환`() {
        val actual = Gender.from("남자")

        actual shouldBe Gender.MALE
    }

    @Test
    fun `올바르지 않은 성별 값이 주어지면 예외를 반환`() {
        shouldThrow<BadRequestException> {
            SubPosition.from("invalid")
        }
    }
}
