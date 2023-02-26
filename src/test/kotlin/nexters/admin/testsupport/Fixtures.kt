package nexters.admin.testsupport

import nexters.admin.controller.user.CreateMemberRequest
import nexters.admin.controller.user.UpdateMemberRequest
import nexters.admin.domain.attendance.Attendance
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.attendance.QrCode
import nexters.admin.domain.generation.Generation
import nexters.admin.domain.generation.GenerationStatus
import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.generation_member.Position
import nexters.admin.domain.generation_member.SubPosition
import nexters.admin.domain.session.Session
import nexters.admin.domain.user.Password
import nexters.admin.domain.user.administrator.Administrator
import nexters.admin.domain.user.member.Gender
import nexters.admin.domain.user.member.Member
import nexters.admin.domain.user.member.MemberStatus
import java.time.LocalDate
import java.time.LocalDateTime

const val ADMIN_USERNAME = "admin"
const val PHONE_NUMBER = "01012345678"

fun createNewMember(
        name: String = "정진우",
        password: String = "1234",
        email: String = "jwjeong@gmail.com",
        gender: Gender = Gender.MALE,
        phoneNumber: String = PHONE_NUMBER,
        status: MemberStatus = MemberStatus.NOT_COMPLETION,
        hasChangedPassword: Boolean = false,
): Member {
    return Member(name, Password(password), email, gender, phoneNumber, status, hasChangedPassword)
}

fun generateCreateMemberRequest(
        name: String = "김태현",
        gender: String = "남자",
        email: String = "kth990303@naver.com",
        phoneNumber: String = "01012345678",
        generation: MutableList<Int> = mutableListOf(22),
        position: String = "개발자",
        subPosition: String = "백엔드",
        status: String = "미이수",
): CreateMemberRequest {
    return CreateMemberRequest(name, gender, email, phoneNumber, generation, position, subPosition, status)
}

fun createNewGenerationMember(
        memberId: Long = 0L,
        generation: Int = 22,
        position: Position = Position.DEVELOPER,
        subPosition: SubPosition = SubPosition.BE,
        score: Int = 100,
        isCompletable: Boolean = true,
): GenerationMember {
    return GenerationMember(memberId, generation, position, subPosition, score, isCompletable)
}

fun createUpdateMemberRequest(
        name: String = "김태현",
        gender: String = "남자",
        email: String = "kth990303@naver.com",
        phoneNumber: String = "01012345678",
        generations: MutableList<Int> = mutableListOf(21),
): UpdateMemberRequest {
    return UpdateMemberRequest(
            name = name,
            gender = gender,
            email = email,
            phoneNumber = phoneNumber,
            generations = generations
    )
}

fun createNewAdmin(
        username: String = ADMIN_USERNAME,
        password: String = "1234",
        lastAccessTime: LocalDateTime = LocalDateTime.now(),
): Administrator {
    return Administrator(username, Password(password), lastAccessTime)
}

fun createNewSession(
        title: String = "OT",
        description: String = "오늘은 설레는 첫 세션 날이에요!",
        generation: Int = 22,
        sessionTime: LocalDate = LocalDate.of(2023, 1, 7),
        week: Int = 1,
        startAttendTime: LocalDateTime? = LocalDateTime.of(2023, 1, 7, 14, 0),
        endAttendTime: LocalDateTime? = LocalDateTime.of(2023, 1, 7, 14, 5),
): Session {
    return Session(title, description, generation, sessionTime, week, startAttendTime, endAttendTime)
}

fun createNewAttendance(
        attendTime: LocalDateTime = LocalDateTime.of(2023, 1, 7, 14, 3),
        generationMemberId: Long = 0L,
        sessionId: Long = 0L,
        attendanceStatus: AttendanceStatus = AttendanceStatus.ATTENDED,
        scoreChanged: Int = 0
): Attendance {
    return Attendance(
            attendTime = attendTime,
            generationMemberId = generationMemberId,
            sessionId = sessionId,
            attendanceStatus = attendanceStatus,
            scoreChanged = scoreChanged
    )
}

fun createNewPendingAttendance(
        generationMemberId: Long = 0L,
        sessionId: Long = 0L,
): Attendance {
    return Attendance(
            generationMemberId = generationMemberId,
            sessionId = sessionId,
            attendanceStatus = AttendanceStatus.PENDING,
    )
}

fun createNewQrCode(
        sessionId: Long = 1L,
        value: String = "ASDFGH",
        attendanceType: AttendanceStatus = AttendanceStatus.ATTENDED,
        expirationTime: LocalDateTime = LocalDateTime.now().plusSeconds(60),
): QrCode {
    return QrCode(sessionId, value, attendanceType, expirationTime)
}

fun createNewGeneration(
        generation: Int = 22,
        status: String = "활동 중",
): Generation {
    return Generation(generation, GenerationStatus.from(status))
}

fun createExcelInput() = mutableMapOf(
        "name" to mutableListOf("정진우", "김민수", "최다예"),
        "gender" to mutableListOf("남자", "남자", "여자"),
        "email" to mutableListOf("jinwoo@gmail.com", "ming@gmail.com", "dayeah@gmail.com"),
        "phone_number" to mutableListOf("01012345678", "01012345679", "01012345670"),
        "position" to mutableListOf("개발자", "운영진", "디자이너"),
        "sub_position" to mutableListOf("프론트엔드", "CTO", ""),
        "status" to mutableListOf("미이수", "수료", "제명")
)
