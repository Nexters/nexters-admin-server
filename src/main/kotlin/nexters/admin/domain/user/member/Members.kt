package nexters.admin.domain.user.member

import nexters.admin.exception.BadRequestException

private const val EMAIL = "email"
private const val NAME = "name"
private const val GENDER = "gender"
private const val PHONE_NUMBER = "phone_number"
private const val STATUS = "status"

val REQUIRED_KEYS = listOf(NAME, GENDER, EMAIL, PHONE_NUMBER, STATUS)

// TODO: add tests
class Members(
        private val values: Map<String, Member>
) {
    companion object {
        fun of(members: Map<String, List<String>>): Members {
            validate(members)
            val values = mutableMapOf<String, Member>();
            for (idx in 0 until members[EMAIL]!!.size) {
                val email = members[EMAIL]?.get(idx) ?: throw BadRequestException.missingInfo("회원의 이메일")
                values[email] = (Member.of(
                        name = members[NAME]?.get(idx) ?: throw BadRequestException.missingInfo("회원의 이름"),
                        email = email,
                        phoneNumber = members[PHONE_NUMBER]?.get(idx)
                                ?: throw BadRequestException.missingInfo("회원의 전화번호"),
                        gender = members[GENDER]?.get(idx) ?: throw BadRequestException.missingInfo("회원의 성별"),
                        status = members[STATUS]?.get(idx) ?: throw BadRequestException.missingInfo("회원의 활동구분"),
                ))
            }
            return Members(values.toMap())
        }

        private fun validate(members: Map<String, List<String>>) {
            if (members.isEmpty()) {
                throw BadRequestException.wrongCsvFile()
            }
            if (!members.keys.containsAll(REQUIRED_KEYS)) {
                throw BadRequestException.wrongCsvFile()
            }
            if (members[EMAIL]!!.size != setOf(members[EMAIL]).size) {
                throw BadRequestException.duplicateEmail()
            }
        }
    }

    fun getEmails(): List<String> {
        return values.keys.toList()
    }

    fun findAllByEmailNotIn(emails: List<String>): List<Member> {
        val members = values.values.toMutableList()
        for (email in emails) {
            values[email]?.let { members.remove(it) }
        }
        return members
    }

    fun updateMembersWithMatchingEmail(curMembers: List<Member>) {
        for (member in curMembers) {
            values[member.email]?.let { newMember ->
                member.update(
                        name = newMember.name,
                        gender = newMember.gender,
                        phoneNumber = newMember.phoneNumber,
                        status = newMember.status,
                )
            }
        }
    }
}
