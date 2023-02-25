package nexters.admin.acceptance

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import nexters.admin.controller.user.UpdateMemberPositionRequest
import nexters.admin.controller.user.UpdateMemberStatusRequest
import nexters.admin.controller.user.UpdatePasswordRequest
import nexters.admin.domain.user.member.MemberStatus
import nexters.admin.exception.ExceptionResponse
import nexters.admin.service.auth.MemberLoginRequest
import nexters.admin.service.auth.MemberLoginResponse
import nexters.admin.service.user.FindProfileResponse
import nexters.admin.testsupport.AcceptanceTest
import nexters.admin.testsupport.createUpdateMemberRequest
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
    fun `관리자가 회원 생성시 필드 형식이 잘못되면 예외를 응답한다`() {
        val adminToken = 관리자_생성_토큰_발급()
        val invalidRequest = generateCreateMemberRequest(email = "asd", phoneNumber = "123456789012345678901234567890")

        Given {
            log().all()
            contentType(MediaType.APPLICATION_JSON_VALUE)
            auth().oauth2(adminToken)
            body(invalidRequest)
        } When {
            post("/api/members")
        } Then {
            statusCode(400)
        } Extract {
            `as`(ExceptionResponse::class.java).message shouldStartWith "잘못된 입력입니다: "
        }
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
    fun `비밀번호 변경 시 8~20자로 변경하면 성공한다`() {
        val adminToken = 관리자_생성_토큰_발급()
        val request = generateCreateMemberRequest()
        회원_생성(adminToken, request)
        val memberToken = 회원_로그인_토큰(request.email, "12345678")

        Given {
            log().all()
            contentType(MediaType.APPLICATION_JSON_VALUE)
            auth().oauth2(memberToken)
            body(UpdatePasswordRequest("12345678!"))
        } When {
            put("/api/members/password")
        } Then {
            statusCode(200)
        }
    }

    @Test
    fun `변경한 비밀번호로 다시 로그인할 수 있다`() {
        val adminToken = 관리자_생성_토큰_발급()
        val request = generateCreateMemberRequest()
        회원_생성(adminToken, request)
        val memberToken = 회원_로그인_토큰(request.email, "12345678")
        비밀번호_수정(memberToken, "12345678!")

        Given {
            log().all()
            contentType(MediaType.APPLICATION_JSON_VALUE)
            body(MemberLoginRequest(request.email, "12345678!"))
        } When {
            post("/api/auth/login/member")
        } Then {
            statusCode(200)
        } Extract {
            `as`(MemberLoginResponse::class.java).token shouldNotBe null
        }
    }

    @Test
    fun `관리자가 존재하지 않는 회원을 수정 및 기수회원을 동기화하려는 경우 예외를 응답한다`() {
        val adminToken = 관리자_생성_토큰_발급()
        Given {
            log().all()
            contentType(MediaType.APPLICATION_JSON_VALUE)
            auth().oauth2(adminToken)
            body(createUpdateMemberRequest())
        } When {
            put("/api/members/999")
        } Then {
            statusCode(404)
        } Extract {
        }
    }

    @Test
    fun `관리자가 존재하지 않는 회원의 활동구분을 수정하려는 경우 예외를 응답한다`() {
        val adminToken = 관리자_생성_토큰_발급()
        Given {
            log().all()
            contentType(MediaType.APPLICATION_JSON_VALUE)
            auth().oauth2(adminToken)
            body(UpdateMemberStatusRequest(MemberStatus.EXPULSION.value))
        } When {
            put("/api/members/999/status")
        } Then {
            statusCode(404)
        } Extract {
        }
    }

    @Test
    fun `관리자가 존재하지 않는 회원의 직군을 수정하려는 경우 예외를 응답한다`() {
        val adminToken = 관리자_생성_토큰_발급()
        Given {
            log().all()
            contentType(MediaType.APPLICATION_JSON_VALUE)
            auth().oauth2(adminToken)
            body(UpdateMemberPositionRequest("디자이너", null))
        } When {
            put("/api/members/999/position")
        } Then {
            statusCode(404)
        } Extract {
        }
    }

    @Test
    fun `관리자가 존재하지 않는 회원을 삭제하려는 경우 예외를 응답한다`() {
        val adminToken = 관리자_생성_토큰_발급()
        Given {
            log().all()
            auth().oauth2(adminToken)
        } When {
            delete("/api/members/999")
        } Then {
            statusCode(404)
        } Extract {
        }
    }
}
