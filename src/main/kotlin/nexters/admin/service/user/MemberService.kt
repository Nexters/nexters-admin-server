package nexters.admin.service.user

import nexters.admin.domain.user.Password
import nexters.admin.domain.user.member.Member
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class MemberService(
        private val memberRepository: MemberRepository,
        private val generationMemberRepository: GenerationMemberRepository,
) {
    @Transactional(readOnly = true)
    fun findAllByAdministrator(): FindAllMembersResponse {
        return FindAllMembersResponse(
                memberRepository.findAll()
                        .map {
                            val generationMembers = generationMemberRepository.findAllByMemberId(it.id)
                            FindMemberResponse.of(it, generationMembers)
                        })
    }

    fun updatePassword(loggedInMember: Member, newPassword: Password) {
        loggedInMember.updatePassword(newPassword)
        memberRepository.save(loggedInMember)
    }

    @Transactional(readOnly = true)
    fun getByEmail(email: String): Member {
        return memberRepository.findByEmail(email)
                ?: throw NotFoundException.memberNotFound()
    }
}
