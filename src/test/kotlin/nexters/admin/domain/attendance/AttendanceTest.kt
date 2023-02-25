package nexters.admin.domain.attendance

import io.kotest.matchers.shouldBe
import nexters.admin.testsupport.createNewAttendance
import org.junit.jupiter.api.Test

class AttendanceTest {

    @Test
    fun `QR 코드 혹은 관리자에 의한 출석 상태 수정시 기존 출석 상태를 기준으로 점수 변화`() {
        val attendance = createNewAttendance()

        attendance.updateStatusByAdmin(AttendanceStatus.AUTHORIZED_ABSENCE, "통보 결석")
        attendance.updateStatusByQr(AttendanceStatus.TARDY)
        attendance.updateStatusByAdmin(AttendanceStatus.TARDY, "지각 확정")

        attendance.scoreChanged shouldBe AttendanceStatus.TARDY.penaltyScore
        attendance.note shouldBe "지각 확정"
    }

    @Test
    fun `추가 점수 및 기타 점수 설명 입력시, 해당 점수만큼 점수 변화`() {
        val attendance = createNewAttendance()
        val extraScoreChange = 5

        attendance.updateStatusByQr(AttendanceStatus.AUTHORIZED_ABSENCE)
        attendance.addExtraScore(extraScoreChange, "운영 지원")
        attendance.updateStatusByAdmin(AttendanceStatus.AUTHORIZED_ABSENCE, "변화 없음")

        attendance.scoreChanged shouldBe extraScoreChange + AttendanceStatus.AUTHORIZED_ABSENCE.penaltyScore
        attendance.extraScoreNote shouldBe "운영 지원"
    }
}
