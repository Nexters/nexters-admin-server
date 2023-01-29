package nexters.admin.domain.attendance

enum class AttendanceStatus(val value: String) {
    PENDING("대기"),
    ATTENDED("출석"),
    TARDY("지각"),
    UNAUTHORIZED_ABSENCE("무단결석"),
    AUTHORIZED_ABSENCE("통보결석"),
}
