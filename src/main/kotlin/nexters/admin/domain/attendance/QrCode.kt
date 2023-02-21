package nexters.admin.domain.attendance

import nexters.admin.exception.BadRequestException
import nexters.admin.support.utils.randomStringLengthOf
import java.time.LocalDateTime

const val LENGTH = 6

class QrCode(
        val sessionId: Long,
        val value: String = randomStringLengthOf(LENGTH),
        val type: AttendanceStatus,
        val expirationTime: LocalDateTime
) {
    init {
        if (type != AttendanceStatus.ATTENDED && type != AttendanceStatus.TARDY) {
            throw BadRequestException.wrongQrCodeType()
        }
    }

    companion object {
        fun of(sessionId: Long, attendanceType: AttendanceStatus, expirationTime: LocalDateTime): QrCode {
            return QrCode(sessionId = sessionId, type = attendanceType, expirationTime = expirationTime)
        }
    }

    fun isSameValue(value: String): Boolean {
        return this.value == value
    }

    fun isExpired(): Boolean {
        return expirationTime.isBefore(LocalDateTime.now())
    }
}
