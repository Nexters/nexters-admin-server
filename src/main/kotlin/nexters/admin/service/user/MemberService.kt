package nexters.admin.service.user

import nexters.admin.exception.NotFoundException
import nexters.admin.domain.user.Password
import nexters.admin.domain.user.member.Member
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class MemberService(
        private val memberRepository: MemberRepository,
        private val generationMemberRepository: GenerationMemberRepository
) {
    fun updatePassword(loggedInMember: Member, newPassword: Password) {
        loggedInMember.updatePassword(newPassword)
        memberRepository.save(loggedInMember)
    }

    @Transactional(readOnly = true)
    fun getByEmail(email: String): Member {
        return memberRepository.findByEmail(email)
                ?: throw NotFoundException.memberNotFound()
    }

    fun getProfile(loggedInMember: Member): FindProfileResponse {
        val generationMember = generationMemberRepository.findTopByMemberIdOrderByGenerationDesc(loggedInMember.id)
        return FindProfileResponse.of(loggedInMember, generationMember)
    }
}
