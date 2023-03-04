package nexters.admin.controller.attendance

import com.fasterxml.jackson.annotation.JsonFormat
import nexters.admin.domain.attendance.QrCode
import nexters.admin.domain.session.Session
import java.time.LocalDate
import java.time.LocalDateTime

data class ValidateQrCodeRequest(
        val nonce: String,
)

data class CurrentQrCodeResponse(
        val sessionId: Long,
        val sessionDate: LocalDate,
        val week: Int,
        val qrCode: String,
        val qrCodeType: String,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        val expirationTime: LocalDateTime,
) {
    companion object {
        fun from(qrCode: QrCode, session: Session): CurrentQrCodeResponse {
            return CurrentQrCodeResponse(
                    qrCode.sessionId,
                    session.sessionDate,
                    session.week,
                    qrCode.value,
                    qrCode.type.name,
                    qrCode.expirationTime
            )
        }
    }
}

data class ExtraAttendanceScoreChangeRequest(
        val extraScoreChange: Int,
        val extraScoreNote: String?,
)

data class UpdateAttendanceStatusRequest(
        val attendanceStatus: String,
        val note: String?,
)

data class InitializeQrCodesRequest(
        val sessionId: Long,
        val qrCodeType: String,
)
