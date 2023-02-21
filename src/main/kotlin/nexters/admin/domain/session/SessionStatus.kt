package nexters.admin.domain.session

enum class SessionStatus(val value: String) {
    PENDING("출설 체크 시작 전"),
    ONGOING("출석 중"),
    EXPIRED("출석 종료"),
}
