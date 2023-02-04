package nexters.admin

import nexters.admin.domain.user.Password
import nexters.admin.domain.user.member.Gender
import nexters.admin.domain.user.member.Member
import nexters.admin.domain.user.member.MemberStatus

fun createNewMember(): Member = Member("정진우", Password("1234"), "jweong@gmail.com", Gender.MALE, "01012345678", MemberStatus.NOT_COMPLETION, true)
