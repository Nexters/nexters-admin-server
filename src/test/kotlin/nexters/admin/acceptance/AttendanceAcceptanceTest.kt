package nexters.admin.acceptance

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.generation.GenerationStatus
import nexters.admin.domain.user.member.MemberStatus
import nexters.admin.service.attendance.AttendanceSessionResponses
import nexters.admin.testsupport.AcceptanceTest
import nexters.admin.testsupport.generateCreateMemberRequest
import nexters.admin.testsupport.generateCreateSessionRequest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class AttendanceAcceptanceTest : AcceptanceTest() {

    @Test
    fun `해당 세션에 대한 출석 진행 도중 출석 정보 조회`() {
        val adminToken = 관리자_생성_토큰_발급()
        기수_생성(adminToken, 21)
        기수_상태_변경(adminToken, 21, GenerationStatus.FINISH_ACTIVITY.value!!)
        기수_생성(adminToken, 22)
        기수_상태_변경(adminToken, 22, GenerationStatus.DURING_ACTIVITY.value!!)
        val memberToken1 = 회원_생성_토큰_발급(adminToken, generateCreateMemberRequest(
                email = "kth@kth.com", generation = mutableListOf(21, 22), status = MemberStatus.COMPLETION.value))
        val memberToken2 = 회원_생성_토큰_발급(adminToken, generateCreateMemberRequest(name = "정진우", email = "jjw@jjw.com"))
        val sessionId = 세션_생성(adminToken, generateCreateSessionRequest()).sessionId

        val qrCode1 = 출석_시작_및_qr_조회(adminToken, sessionId, AttendanceStatus.ATTENDED).qrCode
        회원_출석(memberToken1, qrCode1)
        val qrCode2 = 출석_시작_및_qr_조회(adminToken, sessionId, AttendanceStatus.TARDY).qrCode
        회원_출석(memberToken2, qrCode2)

        val attendanceSessionResponses = Given {
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

        attendanceSessionResponses.attended shouldBe 1
        attendanceSessionResponses.tardy shouldBe 1
        attendanceSessionResponses.absence shouldBe 0
        attendanceSessionResponses.data.map { it.initialGeneration } shouldContainExactly listOf(21, 22)
    }

    @Test
    fun `해당 세션에 대한 출석 종료 후 출석 정보 조회`() {
        val adminToken = 관리자_생성_토큰_발급()
        기수_생성(adminToken, 21)
        기수_상태_변경(adminToken, 21, GenerationStatus.FINISH_ACTIVITY.value!!)
        기수_생성(adminToken, 22)
        기수_상태_변경(adminToken, 22, GenerationStatus.DURING_ACTIVITY.value!!)
        val memberToken1 = 회원_생성_토큰_발급(adminToken, generateCreateMemberRequest(
                email = "kth@kth.com", generation = mutableListOf(21, 22), status = MemberStatus.COMPLETION.value))
        회원_생성_토큰_발급(adminToken, generateCreateMemberRequest(name = "정진우", email = "jjw@jjw.com"))
        val sessionId = 세션_생성(adminToken, generateCreateSessionRequest()).sessionId

        val qrCode = 출석_시작_및_qr_조회(adminToken, sessionId, AttendanceStatus.ATTENDED).qrCode
        회원_출석(memberToken1, qrCode)
        출석_시작_및_qr_조회(adminToken, sessionId, AttendanceStatus.TARDY).qrCode
        출석_종료(adminToken)

        val attendanceSessionResponses = Given {
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

        attendanceSessionResponses.attended shouldBe 1
        attendanceSessionResponses.tardy shouldBe 0
        attendanceSessionResponses.absence shouldBe 1
        attendanceSessionResponses.data.map { it.initialGeneration } shouldContainExactly listOf(21, 22)
    }
}
