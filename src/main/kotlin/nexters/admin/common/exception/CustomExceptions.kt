package nexters.admin.common.exception

class BadRequestException(message: String?) : RuntimeException(message)
class UnauthenticatedException(message: String?) : RuntimeException(message) {
    companion object {
        fun loginFail(): UnauthenticatedException = UnauthenticatedException("로그인에 실패하였습니다.")
    }
}
class ForbiddenException(message: String?) : RuntimeException(message)
class NotFoundException(message: String?) : RuntimeException(message)
