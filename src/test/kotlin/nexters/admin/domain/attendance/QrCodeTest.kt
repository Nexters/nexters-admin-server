package nexters.admin.domain.attendance

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nexters.admin.exception.BadRequestException
import nexters.admin.testsupport.createNewQrCode
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class QrCodeTest {

    @Test
    fun `출석 혹은 지각 이외의 타입으로 코드 생성시 예외 발생`() {
        shouldThrow<BadRequestException> {
            createNewQrCode(attendanceType = AttendanceStatus.UNAUTHORIZED_ABSENCE)
        }
    }

    @Test
    fun `유효기간을 기준으로 만료 여부 반환`() {
        val actual = createNewQrCode(expirationTime = LocalDateTime.now().minusSeconds(1))

        actual.isExpired() shouldBe true
    }

    @Test
    fun `of 메서드를 통해 임의의 6글자짜리 코드 생성`() {
        val actual =  QrCode.of(1L, AttendanceStatus.ATTENDED, LocalDateTime.now())

        actual.value matches Regex("^[0-9a-zA-Z]+$")
    }
}
