package nexters.admin.domain.generation

import nexters.admin.exception.BadRequestException

enum class GenerationStatus(val value: String?) {
    BEFORE_ACTIVITY("활동 준비"),
    DURING_ACTIVITY("활동중"),
    FINISH_ACTIVITY("활동 종료"),
    NULL("")
    ;

    companion object {
        fun from(value: String?): GenerationStatus =
                values().firstOrNull { it.value == value } ?: throw BadRequestException.wrongPosition()
    }
}
