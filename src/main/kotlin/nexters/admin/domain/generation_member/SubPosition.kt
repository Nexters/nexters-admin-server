package nexters.admin.domain.generation_member

enum class SubPosition(val value: String) {
    BE("백엔드"),
    FE("프론트엔드"),
    ANDROID("안드로이드"),
    IOS("IOS"),
    DESIGNER("디자이너"),
    ;

    companion object {
        fun from(value: String): SubPosition = values().first { it.value == value }
    }
}
