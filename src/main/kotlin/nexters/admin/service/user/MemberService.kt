package nexters.admin.service.user

import nexters.admin.common.exception.NotFoundException
import nexters.admin.domain.user.Password
import nexters.admin.domain.user.member.Member
import nexters.admin.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class MemberService(
        private val memberRepository: MemberRepository
) {
    fun updatePassword(loggedInMember: Member, newPassword: Password) {
        loggedInMember.updatePassword(newPassword)
        memberRepository.save(loggedInMember)
    }

    fun getByEmail(email: String): Member {
        return memberRepository.findByEmail(email)
                ?: throw NotFoundException.memberNotFound()
    }
}
