package nexters.admin.domain.user.member

import nexters.admin.exception.BadRequestException

enum class Gender(val value: String) {
    MALE("남자"),
    FEMALE("여자"),
    ;

    companion object {
        fun from(value: String): Gender =
                values().firstOrNull { it.value == value } ?: throw BadRequestException.wrongGender()
    }
}
