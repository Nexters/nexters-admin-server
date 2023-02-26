package nexters.admin.domain.attendance

import nexters.admin.exception.BadRequestException

enum class AttendanceStatus(val value: String, val penaltyScore: Int) {
    PENDING("대기", 0),
    ATTENDED("출석", 0),
    TARDY("지각", -5),
    UNAUTHORIZED_ABSENCE("무단결석", -15),
    AUTHORIZED_ABSENCE("통보결석", -10),
    ;

    companion object {
        fun from(name: String): AttendanceStatus = values()
                .firstOrNull { it.name == name }
                ?: throw BadRequestException.wrongAttendanceStatus()
    }
}
