package nexters.admin.repository

import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.attendance.QrCode
import org.springframework.stereotype.Component
import java.time.LocalDateTime

const val BACKUP_CODE_COUNT = 10

@Component
class QrCodeRepository {

    private var qrCodes: MutableList<QrCode> = ArrayList()

    fun getQrCodes(): List<QrCode> {
        return this.qrCodes.toList()
    }

    fun getCurrentSessionId(): Long? {
        return qrCodes.firstOrNull()?.sessionId
    }

    fun findCurrentValidCode(): QrCode? {
        return qrCodes.firstOrNull { !it.isExpired() }
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
            initializeCodes(existingCode.sessionId, existingCode.type)
            return
        }
        while (qrCodes.size < BACKUP_CODE_COUNT) {
            val lastCode = qrCodes[qrCodes.size - 1]
            qrCodes.add(QrCode.of(lastCode.sessionId, lastCode.type, lastCode.expirationTime.plusMinutes(1)))
        }
    }

    fun clear() {
        qrCodes.clear()
    }
}
