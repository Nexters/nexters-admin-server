package nexters.admin.common.exception

class BadRequestException(message: String?) : RuntimeException(message)

class UnauthenticatedException(message: String?) : RuntimeException(message) {
    companion object {
        fun loginFail() = UnauthenticatedException("로그인에 실패하였습니다.")
        fun loginNeeded() = UnauthenticatedException("다시 로그인해주시기 바랍니다.")
    }
}

class ForbiddenException(message: String?) : RuntimeException(message)

class NotFoundException(message: String?) : RuntimeException(message)
