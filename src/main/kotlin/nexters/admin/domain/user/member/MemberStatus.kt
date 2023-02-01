package nexters.admin.domain.user.member

enum class MemberStatus(val value: String) {
    NOT_COMPLETION("미이수"),
    COMPLETION("이수"),
    CERTIFICATED("수료"),
    EXPULSION("제명"),
}
