package nexters.admin.repository

import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.attendance.QrCode
import nexters.admin.exception.NotFoundException
import org.springframework.stereotype.Component
import java.time.LocalDateTime

const val BACKUP_CODE_COUNT = 10

@Component
class QrCodeRepository {

    internal var qrCodes: MutableList<QrCode> = ArrayList()

    fun getCurrentValidCode(): QrCode {
        return qrCodes.firstOrNull { !it.isExpired() } ?: throw NotFoundException.qrCodeNotFound()
    }

    fun initializeCodes(sessionId: Long, type: AttendanceStatus) {
        clear()
        val currentTime = LocalDateTime.now()
        for (minutes: Long in 1L..BACKUP_CODE_COUNT) {
            qrCodes.add(QrCode.of(sessionId, type, currentTime.plusMinutes(minutes)))
        }
    }

    fun updateValidCodes() {
        val existingCode = qrCodes.firstOrNull() ?: return
        qrCodes = qrCodes.filter { !it.isExpired() }.toMutableList()
        if (qrCodes.isEmpty()) {
            initializeCodes(existingCode.sessionId, existingCode.attendanceType)
            return
        }
        while (qrCodes.size < BACKUP_CODE_COUNT) {
            val lastCode = qrCodes[qrCodes.size - 1]
            qrCodes.add(QrCode.of(lastCode.sessionId, lastCode.attendanceType, lastCode.expirationTime.plusMinutes(1)))
        }
    }

    fun clear() {
        qrCodes.clear()
    }
}
