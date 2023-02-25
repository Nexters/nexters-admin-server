package nexters.admin.acceptance

import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import nexters.admin.controller.auth.TokenResponse
import nexters.admin.controller.user.CreateAdministratorRequest
import nexters.admin.controller.user.CreateMemberRequest
import nexters.admin.service.auth.AdminLoginRequest
import nexters.admin.service.auth.MemberLoginRequest
import nexters.admin.service.auth.MemberLoginResponse
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
        `as`(TokenResponse::class.java).data
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
    } Extract { }
}
