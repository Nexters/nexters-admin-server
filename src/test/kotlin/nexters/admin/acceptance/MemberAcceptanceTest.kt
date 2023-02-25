package nexters.admin.acceptance

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import nexters.admin.controller.user.UpdatePasswordRequest
import nexters.admin.service.user.FindProfileResponse
import nexters.admin.testsupport.AcceptanceTest
import nexters.admin.testsupport.generateCreateMemberRequest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class MemberAcceptanceTest : AcceptanceTest() {

    @Test
    fun `관리자는 회원을 생성할 수 있다`() {
        val adminToken = 관리자_생성_토큰_발급()
        val request = generateCreateMemberRequest()
        회원_생성(adminToken, request)

        val actual = 회원_전체_조회(adminToken)

        actual.data shouldHaveSize 1
        actual.data.first().name shouldBe request.name
    }

    @Test
    fun `일반 회원은 회원 생성을 할 수 없다`() {
        val adminToken = 관리자_생성_토큰_발급()
        val memberToken = 회원_생성_토큰_발급(adminToken, generateCreateMemberRequest())

        Given {
            log().all()
            contentType(MediaType.APPLICATION_JSON_VALUE)
            auth().oauth2(memberToken)
            body(generateCreateMemberRequest(name = "김태태", email = "kthFake@taetae.com"))
        } When {
            post("/api/members")
        } Then {
            statusCode(403)
        }
    }

    @Test
    fun `로그인한 상태로 내 정보를 조회할 수 있다`() {
        val adminToken = 관리자_생성_토큰_발급()
        val request = generateCreateMemberRequest()
        회원_생성(adminToken, request)
        val memberToken = 회원_로그인_토큰(request.email, "12345678")

        val response = Given {
            log().all()
            header("Authorization", "Bearer $memberToken")
            contentType(MediaType.APPLICATION_JSON_VALUE)
        } When {
            get("/api/members/me")
        } Then {
            statusCode(200)
        } Extract {
            `as`(FindProfileResponse::class.java)
        }

        response.name shouldBe request.name
    }

    @Test
    fun `비밀번호 변경 시 8~20자로 변경하면 변경된다`() {
        val adminToken = 관리자_생성_토큰_발급()
        val request = generateCreateMemberRequest()
        회원_생성(adminToken, request)
        val memberToken = 회원_로그인_토큰(request.email, "12345678")

        Given {
            log().all()
            contentType(MediaType.APPLICATION_JSON_VALUE)
            auth().oauth2(memberToken)
            body(UpdatePasswordRequest("12345678"))
        } When {
            put("/api/members/password")
        } Then {
            statusCode(200)
        }
    }
}
