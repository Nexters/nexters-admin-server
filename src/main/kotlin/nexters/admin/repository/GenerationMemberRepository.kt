package nexters.admin.repository

import nexters.admin.domain.generation_member.GenerationMember
import org.springframework.data.jpa.repository.JpaRepository

interface GenerationMemberRepository : JpaRepository<GenerationMember, Long> {
    fun findAllByMemberId(memberId: Long): List<GenerationMember>
}
