package nexters.admin.controller.attendance

import nexters.admin.domain.attendance.QrCode
import java.time.LocalDateTime

data class ValidateQrCodeRequest(
        val nonce: String,
)

data class CurrentQrCodeResponse(
        val sessionId: Long,
        val qrCode: String,
        val qrCodeType: String,
        val expirationTime: LocalDateTime,
) {
    companion object {
        fun from(qrCode: QrCode): CurrentQrCodeResponse {
            return CurrentQrCodeResponse(qrCode.sessionId, qrCode.value, qrCode.type.name, qrCode.expirationTime)
        }
    }
}

data class InitializeQrCodesRequest(
        val sessionId: Long,
        val qrCodeType: String,
)
