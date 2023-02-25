package nexters.admin.controller.attendance

import com.fasterxml.jackson.annotation.JsonFormat
import nexters.admin.domain.attendance.QrCode
import java.time.LocalDateTime

data class ValidateQrCodeRequest(
        val nonce: String,
)

data class CurrentQrCodeResponse(
        val sessionId: Long,
        val qrCode: String,
        val qrCodeType: String,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        val expirationTime: LocalDateTime,
) {
    companion object {
        fun from(qrCode: QrCode): CurrentQrCodeResponse {
            return CurrentQrCodeResponse(qrCode.sessionId, qrCode.value, qrCode.type.name, qrCode.expirationTime)
        }
    }
}

data class UpdateAttendanceStatusRequest(
        val attendanceStatus: String,
        val note: String?
)

data class InitializeQrCodesRequest(
        val sessionId: Long,
        val qrCodeType: String,
)
