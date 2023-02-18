package nexters.admin.domain.attendance

import nexters.admin.exception.BadRequestException
import java.time.LocalDateTime

class QrCode(
        val sessionId: Long,
        val value: String,
        val attendanceType: AttendanceStatus,
        val expirationTime: LocalDateTime
) {
    init {
        if (attendanceType != AttendanceStatus.ATTENDED && attendanceType != AttendanceStatus.TARDY) {
            throw BadRequestException.wrongQrCodeType()
        }
    }

    fun isExpired(): Boolean {
        return expirationTime.isBefore(LocalDateTime.now())
    }
}
