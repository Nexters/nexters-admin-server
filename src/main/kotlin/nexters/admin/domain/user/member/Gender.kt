package nexters.admin.domain.user.member

enum class Gender(val value: String) {
    MALE("남자"),
    FEMALE("여자"),
    ;

    companion object {
        fun from(value: String): Gender = values().first { it.value == value }
    }
}
