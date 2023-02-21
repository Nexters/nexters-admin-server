package nexters.admin.service.attendance

import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.attendance.QrCode
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.QrCodeRepository
import org.springframework.stereotype.Service

@Service
class QrCodeService(
        private val qrCodeRepository: QrCodeRepository,
) {
    fun getCurrentQrCode(): QrCode {
        return qrCodeRepository.findCurrentValidCode()
                ?: throw NotFoundException.qrCodeNotFound()
    }

    fun initializeCodes(sessionId: Long, qrCodeType: String) {
        qrCodeRepository.initializeCodes(sessionId, AttendanceStatus.from(qrCodeType))
    }
}
