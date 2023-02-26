package nexters.admin.service.attendance

import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.attendance.QrCode
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.QrCodeRepository
import nexters.admin.repository.SessionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class QrCodeService(
        private val qrCodeRepository: QrCodeRepository,
        private val sessionRepository: SessionRepository,
) {
    fun getCurrentQrCode(): QrCode {
        return qrCodeRepository.findCurrentValidCode()
                ?: throw NotFoundException.qrCodeNotFound()
    }

    @Transactional
    fun initializeCodes(sessionId: Long, qrCodeType: String) {
        val session = (sessionRepository.findByIdOrNull(sessionId)
                ?: throw NotFoundException.sessionNotFound())
        session.updateStartAttendTime(LocalDateTime.now())
        qrCodeRepository.initializeCodes(sessionId, AttendanceStatus.from(qrCodeType))
    }
}
