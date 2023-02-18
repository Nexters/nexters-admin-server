package nexters.admin.service.attendance

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.exception.NotFoundException
import nexters.admin.testsupport.ApplicationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ApplicationTest
class QrCodeServiceTest(
        @Autowired private val qrCodeService: QrCodeService,
) {
    @Test
    fun `출석 체크 시작 전에 QR 코드 조회 시도시 예외 발생`() {
        shouldThrow<NotFoundException> {
            qrCodeService.getCurrentQrCode()
        }
    }

    @Test
    fun `출석 체크 시작 후에 현재 유효한 QR 코드 조회 가능`() {
        qrCodeService.initializeCodes(1L, AttendanceStatus.ATTENDED.name)

        val actual = qrCodeService.getCurrentQrCode()

        actual.sessionId shouldBe 1L
        actual.type shouldBe AttendanceStatus.ATTENDED
        actual.isExpired() shouldBe false
    }

    @Test
    fun `출석 체크 종료 이후에 유효한 QR 코드 조회 시도시 예외 발생`() {
        qrCodeService.initializeCodes(1L, AttendanceStatus.ATTENDED.name)

        qrCodeService.endAttendance()
        shouldThrow<NotFoundException> {
            qrCodeService.getCurrentQrCode()
        }
    }
    // TODO: 출석 체크 종료시, PENDING 상태는 전부 무단 결석으로 변하는지 검증하는 테스트 코드 필요
}
