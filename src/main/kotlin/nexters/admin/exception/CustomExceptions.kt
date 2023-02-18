package nexters.admin.exception

class BadRequestException(message: String?) : RuntimeException(message) {
    companion object {
        fun alreadyExistsAdministrator() = BadRequestException("이미 존재하는 관리자 아이디입니다.")
        fun wrongGender() = BadRequestException("올바르지 않은 성별입니다.")
        fun wrongPosition() = BadRequestException("올바르지 않은 직군입니다.")
        fun wrongMemberStatus() = BadRequestException("올바르지 않은 활동구분입니다.")
        fun wrongSubPosition() = BadRequestException("올바르지 않은 세부직군입니다.")
        fun wrongQrCodeType() = BadRequestException("QR 코드 타입은 ATTENDED 혹은 TARDY만 허용됩니다.")
    }
}

class UnauthenticatedException(message: String?) : RuntimeException(message) {
    companion object {
        fun loginFail() = UnauthenticatedException("로그인에 실패하였습니다.")
        fun loginNeeded() = UnauthenticatedException("다시 로그인해주시기 바랍니다.")
    }
}

class ForbiddenException(message: String?) : RuntimeException(message)

class NotFoundException(message: String?) : RuntimeException(message) {
    companion object {
        fun memberNotFound() = NotFoundException("존재하지 않는 회원입니다.")
        fun sessionNotFound() = NotFoundException("존재하지 않는 세션입니다.")
    }
}
