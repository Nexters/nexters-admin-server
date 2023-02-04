package nexters.admin.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ExceptionControllerAdvice : ResponseEntityExceptionHandler() {

    @ExceptionHandler
    fun handle(exception: BadRequestException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse(exception.message ?: "잘못된 요청입니다."))
    }

    @ExceptionHandler
    fun handle(exception: UnauthenticatedException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ExceptionResponse(exception.message ?: "인증에 실패하였습니다."))
    }

    @ExceptionHandler
    fun handle(exception: ForbiddenException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ExceptionResponse(exception.message ?: "권한이 없습니다."))
    }

    @ExceptionHandler
    fun handle(exception: NotFoundException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ExceptionResponse(exception.message ?: "존재하지 않는 데이터입니다."))
    }

    @ExceptionHandler
    fun handleGlobalException(exception: Exception): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponse("알 수 없는 예외가 발생했습니다."))
    }
}

data class ExceptionResponse(val message: String)
