package nexters.admin.service.attendance

import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.attendance.QrCode
import nexters.admin.exception.BadRequestException
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.AttendanceRepository
import nexters.admin.repository.QrCodeRepository
import nexters.admin.repository.findAllPendingAttendanceOf
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class QrCodeService(
        private val qrCodeRepository: QrCodeRepository,
        private val attendanceRepository: AttendanceRepository,
) {
    fun getCurrentQrCode(): QrCode {
        return qrCodeRepository.findCurrentValidCode()
                ?: throw NotFoundException.qrCodeNotFound()
    }

    fun initializeCodes(sessionId: Long, qrCodeType: String) {
        qrCodeRepository.initializeCodes(sessionId, AttendanceStatus.from(qrCodeType))
    }

    @Transactional
    fun endAttendance() {
        val activeSessionId = qrCodeRepository.getCurrentSessionId()
                ?: throw BadRequestException.attendanceNotStarted()
        qrCodeRepository.clear()

        val pendingAttendances = attendanceRepository.findAllPendingAttendanceOf(activeSessionId)
        pendingAttendances.forEach {
            it.updateStatus(AttendanceStatus.UNAUTHORIZED_ABSENCE)
        }
    }
}
