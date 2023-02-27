package nexters.admin.acceptance

import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import nexters.admin.controller.attendance.CurrentQrCodeResponse
import nexters.admin.controller.attendance.ExtraAttendanceScoreChangeRequest
import nexters.admin.controller.attendance.InitializeQrCodesRequest
import nexters.admin.controller.attendance.UpdateAttendanceStatusRequest
import nexters.admin.controller.attendance.ValidateQrCodeRequest
import nexters.admin.controller.auth.TokenResponse
import nexters.admin.controller.generation.CreateGenerationRequest
import nexters.admin.controller.generation.UpdateGenerationRequest
import nexters.admin.controller.session.CreateSessionRequest
import nexters.admin.controller.user.CreateAdministratorRequest
import nexters.admin.controller.user.CreateMemberRequest
import nexters.admin.controller.user.UpdatePasswordRequest
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.service.attendance.AttendanceSessionResponses
import nexters.admin.service.attendance.FindAttendanceProfileResponse
import nexters.admin.service.auth.AdminLoginRequest
import nexters.admin.service.auth.MemberLoginRequest
import nexters.admin.service.auth.MemberLoginResponse
import nexters.admin.service.session.CreateSessionResponse
import nexters.admin.service.user.FindAllMembersResponse
import org.springframework.http.MediaType

const val DEFAULT_PASSWORD_LENGTH = 8

fun 회원_전체_조회(adminToken: String): FindAllMembersResponse {
    return Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        auth().oauth2(adminToken)
    } When {
        get("/api/members")
    } Then {
        statusCode(200)
    } Extract {
        `as`(FindAllMembersResponse::class.java)
    }
}

fun 회원_생성_토큰_발급(adminToken: String, request: CreateMemberRequest): String {
    회원_생성(adminToken, request)
    return 회원_로그인_토큰(request.email, createDefaultPassword(request.phoneNumber))
}

private fun createDefaultPassword(phoneNumber: String): String {
    return phoneNumber.substring(phoneNumber.length - DEFAULT_PASSWORD_LENGTH, phoneNumber.length)
}

fun 회원_로그인_토큰(email: String, password: String): String {
    return Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        body(MemberLoginRequest(email, password))
    } When {
        post("/api/auth/login/member")
    } Then {
        statusCode(200)
    } Extract {
        `as`(MemberLoginResponse::class.java).token
    }
}

fun 회원_생성(adminToken: String, request: CreateMemberRequest) {
    Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        auth().oauth2(adminToken)
        body(request)
    } When {
        post("/api/members")
    } Then {
        statusCode(200)
    } Extract { }
}

fun 비밀번호_수정(memberToken: String, newPassword: String) {
    Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        auth().oauth2(memberToken)
        body(UpdatePasswordRequest(newPassword))
    } When {
        put("/api/members/password")
    } Then {
        statusCode(200)
    }
}

fun 관리자_생성_토큰_발급(): String {
    관리자_생성()
    return 관리자_로그인_토큰()
}

fun 관리자_로그인_토큰(): String {
    return Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        body(AdminLoginRequest("test@test.com", "1234"))
    } When {
        post("/api/auth/login/admin")
    } Then {
        statusCode(200)
    } Extract {
        `as`(TokenResponse::class.java).token
    }
}

fun 관리자_생성() {
    Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        body(CreateAdministratorRequest("test@test.com", "1234"))
    } When {
        post("/api/admin")
    } Then {
        statusCode(200)
    }
}

fun 기수_생성(adminToken: String, generation: Int) {
    Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        auth().oauth2(adminToken)
        body(CreateGenerationRequest(generation))
    } When {
        post("/api/generation")
    } Then {
        statusCode(200)
    }
}

fun 기수_상태_변경(adminToken: String, generation: Int, status: String) {
    Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        auth().oauth2(adminToken)
        body(UpdateGenerationRequest(status))
    } When {
        put("/api/generation/{generation}", generation)
    } Then {
        statusCode(200)
    }
}

fun 세션_생성(adminToken: String, request: CreateSessionRequest): CreateSessionResponse {
    return Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        auth().oauth2(adminToken)
        body(request)
    } When {
        post("/api/sessions")
    } Then {
        statusCode(200)
    } Extract {
        `as`(CreateSessionResponse::class.java)
    }
}

fun 회원_출석(memberToken: String, qrCode: String) {
    Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        auth().oauth2(memberToken)
        body(ValidateQrCodeRequest(qrCode))
    } When {
        post("/api/attendance")
    } Then {
        statusCode(200)
    }
}

fun 출석_시작_및_qr_조회(adminToken: String, sessionId: Long, qrCodeType: AttendanceStatus): CurrentQrCodeResponse {
    Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        auth().oauth2(adminToken)
        body(InitializeQrCodesRequest(sessionId, qrCodeType.name))
    } When {
        post("/api/attendance/qr")
    } Then {
        statusCode(200)
    }

    return Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        auth().oauth2(adminToken)
    } When {
        get("/api/attendance/qr")
    } Then {
        statusCode(200)
    } Extract {
        `as`(CurrentQrCodeResponse::class.java)
    }
}

fun 출석_종료(adminToken: String) {
    Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        auth().oauth2(adminToken)
    } When {
        delete("/api/attendance/qr")
    } Then {
        statusCode(200)
    }
}

fun 나의_출석_조회(memberToken: String): FindAttendanceProfileResponse {
    return Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        auth().oauth2(memberToken)
    } When {
        get("/api/attendance/me")
    } Then {
        statusCode(200)
    } Extract {
        `as`(FindAttendanceProfileResponse::class.java)
    }
}

fun 세션_출석_조회(adminToken: String, sessionId: Long): AttendanceSessionResponses {
    return Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        auth().oauth2(adminToken)
    } When {
        get("/api/attendance/{sessionId}", sessionId)
    } Then {
        statusCode(200)
    } Extract {
        `as`(AttendanceSessionResponses::class.java)
    }
}

fun 출석_점수_부여(adminToken: String, attendanceId: Long, extraScoreChange: Int, extraScoreNote: String?) {
    Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        auth().oauth2(adminToken)
        body(ExtraAttendanceScoreChangeRequest(extraScoreChange, extraScoreNote))
    } When {
        post("/api/attendance/{id}/additional-score", attendanceId)
    } Then {
        statusCode(200)
    }
}

fun 출석_수정(adminToken: String, attendanceId: Long, attendanceStatus: String, note: String?) {
    Given {
        log().all()
        contentType(MediaType.APPLICATION_JSON_VALUE)
        auth().oauth2(adminToken)
        body(UpdateAttendanceStatusRequest(attendanceStatus, note))
    } When {
        put("/api/attendance/{id}/status", attendanceId)
    } Then {
        statusCode(200)
    }
}
