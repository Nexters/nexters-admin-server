package nexters.admin.domain.generation_member

import nexters.admin.exception.BadRequestException

enum class SubPosition(val value: String?) {
    BE("백엔드"),
    FE("프론트엔드"),
    ANDROID("안드로이드"),
    IOS("iOS"),
    DESIGNER("디자이너"),
    MANAGER_CEO("CEO"),
    MANAGER_COO("COO"),
    MANAGER_CMO("CMO"),
    MANAGER_CTO("CTO"),
    MANAGER_CDO("CDO"),
    NULL(""),
    ;

    companion object {
        fun from(value: String?): SubPosition {
            if (value == null) {
                return NULL
            }
            return values().firstOrNull { it.value == value }
                    ?: throw BadRequestException.wrongSubPosition()
        }
    }
}
