package nexters.admin.domain.generation_member

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nexters.admin.exception.BadRequestException
import org.junit.jupiter.api.Test

class SubPositionTest {

    @Test
    fun `올바른 세부직군을 반환`() {
        val actual = SubPosition.from("백엔드")

        actual shouldBe SubPosition.BE
    }

    @Test
    fun `올바르지 않은 세부직군 값이 주어지면 예외를 반환`() {
        shouldThrow<BadRequestException> {
            SubPosition.from("invalid")
        }
    }
}
