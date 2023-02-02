package nexters.admin.common.exception

class BadRequestException(message: String?) : RuntimeException(message)
class UnauthenticatedException(message: String?) : RuntimeException(message)
class ForbiddenException(message: String?) : RuntimeException(message)
class NotFoundException(message: String?) : RuntimeException(message)
