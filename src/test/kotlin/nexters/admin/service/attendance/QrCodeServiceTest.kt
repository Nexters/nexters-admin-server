package nexters.admin.service.attendance

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.SessionRepository
import nexters.admin.testsupport.ApplicationTest
import nexters.admin.testsupport.createNewSession
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ApplicationTest
class QrCodeServiceTest(
        @Autowired private val qrCodeService: QrCodeService,
        @Autowired private val sessionRepository: SessionRepository,
) {
    @Test
    fun `출석 체크 시작 전에 QR 코드 조회 시도시 예외 발생`() {
        shouldThrow<NotFoundException> {
            qrCodeService.getCurrentQrCode()
        }
    }

    @Test
    fun `출석 체크 시작 후에 현재 유효한 QR 코드 조회 가능`() {
        val session = sessionRepository.save(createNewSession())
        qrCodeService.initializeCodes(session.id, AttendanceStatus.ATTENDED.name)

        val actual = qrCodeService.getCurrentQrCode()

        actual.sessionId shouldBe 1L
        actual.qrCodeType shouldBe AttendanceStatus.ATTENDED.name
    }
}
