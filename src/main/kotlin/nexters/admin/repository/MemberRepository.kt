package nexters.admin.repository

import nexters.admin.domain.user.member.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByEmail(email: String): Member?
    fun findByName(name: String): Member?
    fun findAllByEmailIn(emails: List<String>): List<Member>
}
