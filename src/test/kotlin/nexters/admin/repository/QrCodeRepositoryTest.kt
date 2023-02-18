package nexters.admin.repository

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.attendance.QrCode
import nexters.admin.exception.NotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class QrCodeRepositoryTest {

    private lateinit var qrCodeRepository: QrCodeRepository

    @BeforeEach
    fun setUp() {
        qrCodeRepository = QrCodeRepository()
    }

    @Test
    fun `코드 초기화 이전에 유효한 QR 코드 조회 시도시 예외 발생`() {
        shouldThrow<NotFoundException> {
            qrCodeRepository.getCurrentValidCode()
        }
    }

    @Test
    fun `QR 코드 초기화시 같은 세션ID와 타입의 QR 코드 10개 생성 (만료기간 1분 간격)`() {
        val sessionId = 1L
        val type = AttendanceStatus.ATTENDED
        qrCodeRepository.initializeCodes(sessionId, type)

        val qrCodes = qrCodeRepository.getQrCodes()
        for (actual in qrCodes) {
            actual.sessionId shouldBe sessionId
            actual.type shouldBe type
            actual.isExpired() shouldBe false
        }
        qrCodes.size shouldBe 10
        validateOneMinuteInterval(qrCodes)
    }

    @Test
    fun `유효성 갱신 시도시, 만료된 QR 코드 제거 및 마지막 QR 코드의 만료기간을 기준으로 1분 단위로 추가 생성 (10개 유지)`() {
        val sessionId = 1L
        val type = AttendanceStatus.ATTENDED
        qrCodeRepository.initializeCodes(sessionId, type)
        qrCodeRepository.updateValidCodes()

        val qrCodes = qrCodeRepository.getQrCodes()
        for (actual in qrCodes) {
            actual.sessionId shouldBe sessionId
            actual.type shouldBe type
            actual.isExpired() shouldBe false
        }
        qrCodes.size shouldBe 10
        validateOneMinuteInterval(qrCodes)
    }

    @Test
    fun `QR 코드가 없는 저장소의 유효성 갱신 시도시, 상태 변화 없이 예외 미발생`() {
        qrCodeRepository.clear()

        shouldNotThrowAny {
            qrCodeRepository.updateValidCodes()
        }
    }

    @Test
    fun `저장소를 비운 이후에 유효한 QR 코드 조회 시도시 예외 발생`() {
        qrCodeRepository.initializeCodes(1L, AttendanceStatus.ATTENDED)

        qrCodeRepository.clear()
        shouldThrow<NotFoundException> {
            qrCodeRepository.getCurrentValidCode()
        }
    }

    private fun validateOneMinuteInterval(qrCodes: List<QrCode>) {
        var prev = qrCodes.first()
        for (idx: Int in 1 until qrCodes.size) {
            prev.expirationTime.plusMinutes(1) shouldBe qrCodes[idx].expirationTime
            prev = qrCodes[idx]
        }
    }
}
