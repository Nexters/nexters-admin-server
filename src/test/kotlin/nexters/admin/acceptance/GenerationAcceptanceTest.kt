package nexters.admin.acceptance

import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import nexters.admin.controller.generation.CreateGenerationRequest
import nexters.admin.testsupport.AcceptanceTest
import nexters.admin.testsupport.generateCreateMemberRequest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class GenerationAcceptanceTest : AcceptanceTest() {

    @Test
    fun `관리자는 기수를 추가할 수 있다`() {
        val adminToken = 관리자_생성_토큰_발급()

        Given {
            log().all()
            contentType(MediaType.APPLICATION_JSON_VALUE)
            auth().oauth2(adminToken)
            body(CreateGenerationRequest(22))
        } When {
            post("/api/generation")
        } Then {
            statusCode(200)
        }
    }

    @Test
    fun `일반 회원은 기수를 추가할 수 없다`() {
        val adminToken = 관리자_생성_토큰_발급()
        val request = generateCreateMemberRequest()
        회원_생성(adminToken, request)
        val memberToken = 회원_로그인_토큰(request.email, "12345678")

        Given {
            log().all()
            contentType(MediaType.APPLICATION_JSON_VALUE)
            auth().oauth2(memberToken)
            body(CreateGenerationRequest(22))
        } When {
            post("/api/generation")
        } Then {
            statusCode(403)
        }
    }
}
