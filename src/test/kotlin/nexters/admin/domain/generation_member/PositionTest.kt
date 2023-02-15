package nexters.admin.domain.generation_member

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nexters.admin.exception.BadRequestException
import org.junit.jupiter.api.Test

class PositionTest {

    @Test
    fun `올바른 직군을 반환`() {
        val actual = Position.from("개발자")

        actual shouldBe Position.DEVELOPER
    }

    @Test
    fun `올바르지 않은 직군 값이 주어지면 예외를 반환`() {
        shouldThrow<BadRequestException> {
            Position.from("invalid")
        }
    }
}
