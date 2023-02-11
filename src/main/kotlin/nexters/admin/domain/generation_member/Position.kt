package nexters.admin.domain.generation_member

enum class Position(val value: String?) {
    DEVELOPER("개발자"),
    DESIGNER("디자이너"),
    MANAGER("운영진"),
    NULL("")
    ;

    companion object {
        fun from(value: String?): Position = values().first { it.value == value }
    }
}
