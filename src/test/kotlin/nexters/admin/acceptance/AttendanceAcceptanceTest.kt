package nexters.admin.acceptance

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import nexters.admin.domain.attendance.AttendanceStatus.ATTENDED
import nexters.admin.domain.attendance.AttendanceStatus.TARDY
import nexters.admin.domain.attendance.AttendanceStatus.UNAUTHORIZED_ABSENCE
import nexters.admin.domain.generation.GenerationStatus
import nexters.admin.domain.user.member.MemberStatus
import nexters.admin.testsupport.AcceptanceTest
import nexters.admin.testsupport.generateCreateMemberRequest
import nexters.admin.testsupport.generateCreateSessionRequest
import org.junit.jupiter.api.Test

class AttendanceAcceptanceTest : AcceptanceTest() {

    @Test
    fun `내 출석 정보 조회`() {
        val adminToken = 관리자_생성_토큰_발급()
        기수_생성(adminToken, 22)
        기수_상태_변경(adminToken, 22, GenerationStatus.DURING_ACTIVITY.value!!)
        val memberToken = 회원_생성_토큰_발급(adminToken, generateCreateMemberRequest())
        val session1Id = 세션_생성(adminToken, generateCreateSessionRequest(week = 1)).sessionId
        val session2Id = 세션_생성(adminToken, generateCreateSessionRequest(week = 2)).sessionId
        val session3Id = 세션_생성(adminToken, generateCreateSessionRequest(week = 3)).sessionId

        val qrCode1 = 출석_시작_및_qr_조회(adminToken, session1Id, ATTENDED).qrCode
        회원_출석(memberToken, qrCode1)
        출석_종료(adminToken)
        val qrCode2 = 출석_시작_및_qr_조회(adminToken, session2Id, TARDY).qrCode
        회원_출석(memberToken, qrCode2)
        출석_종료(adminToken)
        val qrCode3 = 출석_시작_및_qr_조회(adminToken, session3Id, ATTENDED).qrCode
        회원_출석(memberToken, qrCode3)

        val findAttendanceProfileResponse = 나의_출석_조회(memberToken)

        findAttendanceProfileResponse.isGenerationMember shouldBe true
        findAttendanceProfileResponse.attendanceData?.attendances?.shouldHaveSize(3)
    }

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

        val qrCode1 = 출석_시작_및_qr_조회(adminToken, sessionId, ATTENDED).qrCode
        회원_출석(memberToken1, qrCode1)
        val qrCode2 = 출석_시작_및_qr_조회(adminToken, sessionId, TARDY).qrCode
        회원_출석(memberToken2, qrCode2)

        val attendanceSessionResponses = 세션_출석_조회(adminToken, sessionId)

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

        val qrCode = 출석_시작_및_qr_조회(adminToken, sessionId, ATTENDED).qrCode
        회원_출석(memberToken1, qrCode)
        출석_시작_및_qr_조회(adminToken, sessionId, TARDY).qrCode
        출석_종료(adminToken)

        val attendanceSessionResponses = 세션_출석_조회(adminToken, sessionId)

        attendanceSessionResponses.attended shouldBe 1
        attendanceSessionResponses.tardy shouldBe 0
        attendanceSessionResponses.absence shouldBe 1
        attendanceSessionResponses.data.map { it.initialGeneration } shouldContainExactly listOf(21, 22)
    }

    @Test
    fun `무단결석 처리된 출석을 지각으로 수정`() {
        val adminToken = 관리자_생성_토큰_발급()
        기수_생성(adminToken, 22)
        기수_상태_변경(adminToken, 22, GenerationStatus.DURING_ACTIVITY.value!!)
        val memberToken = 회원_생성_토큰_발급(adminToken, generateCreateMemberRequest())
        val session1Id = 세션_생성(adminToken, generateCreateSessionRequest(week = 1)).sessionId
        val session2Id = 세션_생성(adminToken, generateCreateSessionRequest(week = 2)).sessionId
        출석_시작_및_qr_조회(adminToken, session1Id, ATTENDED)
        출석_종료(adminToken)
        출석_시작_및_qr_조회(adminToken, session2Id, ATTENDED)
        출석_종료(adminToken)

        val attendance1Id = 세션_출석_조회(adminToken, session1Id).data[0].attendanceId
        출석_수정(adminToken, attendance1Id, TARDY.name, "지각 사후 통보")
        val attendance = 나의_출석_조회(memberToken).attendanceData

        attendance?.score shouldBe 100 - 15 - 5
        attendance?.attendances?.get(0)?.attendanceStatus shouldBe UNAUTHORIZED_ABSENCE
        attendance?.attendances?.get(1)?.attendanceStatus shouldBe TARDY
    }

    @Test
    fun `가산점 부여`() {
        val adminToken = 관리자_생성_토큰_발급()
        기수_생성(adminToken, 22)
        기수_상태_변경(adminToken, 22, GenerationStatus.DURING_ACTIVITY.value!!)
        val memberToken = 회원_생성_토큰_발급(adminToken, generateCreateMemberRequest())
        val sessionId = 세션_생성(adminToken, generateCreateSessionRequest(week = 1)).sessionId
        val qrCode = 출석_시작_및_qr_조회(adminToken, sessionId, ATTENDED).qrCode
        회원_출석(memberToken, qrCode)

        val attendanceId = 세션_출석_조회(adminToken, sessionId).data[0].attendanceId
        출석_점수_부여(adminToken, attendanceId, 5, "운영 지원")
        val attendance = 나의_출석_조회(memberToken).attendanceData

        attendance?.score shouldBe 100 + 5
        attendance?.attendances?.get(0)?.attendanceStatus shouldBe ATTENDED
    }
}
