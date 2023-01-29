package nexters.admin.domain.attendance

enum class AttendanceStatus(val value: String) {
    PENDING("대기"),
    ATTEND("출석"),
    TARDY("지각"),    // TODO: 미국에서는 TARDY 라고 하나, 우리가 알아보기엔 LATE 가 편함. 네이밍 고민해보자
    UNAUTHORIZED_ABSENCE("무단결석"),
    AUTHORIZED_ABSENCE("통보결석"),
}
