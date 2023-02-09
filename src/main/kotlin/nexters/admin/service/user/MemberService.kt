package nexters.admin.service.user

import nexters.admin.controller.user.UpdateMemberRequest
import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.generation_member.Position
import nexters.admin.domain.generation_member.SubPosition
import nexters.admin.domain.user.Password
import nexters.admin.domain.user.member.Gender
import nexters.admin.domain.user.member.Member
import nexters.admin.domain.user.member.MemberStatus
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.MemberRepository
import org.springframework.data.repository.findByIdOrNull
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

    fun updateMemberByAdministrator(id: Long, updateMemberRequest: UpdateMemberRequest) {
        val findMember = memberRepository.findByIdOrNull(id)
                ?: throw NotFoundException.memberNotFound()
        updateMemberInfo(findMember, updateMemberRequest)
        updateGenerateMemberInfo(updateMemberRequest, findMember)
    }

    private fun updateMemberInfo(findMember: Member, updateMemberRequest: UpdateMemberRequest) {
        findMember.update(
                updateMemberRequest.name,
                Gender.from(updateMemberRequest.gender),
                updateMemberRequest.phoneNumber
        )
    }

    private fun updateGenerateMemberInfo(updateMemberRequest: UpdateMemberRequest, findMember: Member) {
        updateMemberRequest.generations
                .map {
                    // 해당 기수의 기수회원 정보가 존재하지 않으면 기수회원을 생성해줌.
                    generationMemberRepository.findByGenerationAndMemberId(it, findMember.id)
                            ?: generationMemberRepository.save(
                                    GenerationMember(
                                            memberId = findMember.id,
                                            generation = it,
                                            position = null,
                                            subPosition = null
                                    )
                            )
                }
        // 요청으로 들어온 기수회원 외에 다른 기수회원의 정보가 있다면 삭제해줌
        generationMemberRepository.findAllByMemberId(findMember.id)
                .filterNot { updateMemberRequest.generations.contains(it.generation) }
                .map { generationMemberRepository.deleteById(it.id) }
    }

    fun updateStatusByAdministrator(id: Long, status: String) {
        val findMember = memberRepository.findByIdOrNull(id)
                ?: throw NotFoundException.memberNotFound()
        findMember.updateStatus(MemberStatus.from(status))
    }

    fun updateMemberPositionByAdministrator(id: Long, position: String?, subPosition: String?) {
        val findGenerationMember = generationMemberRepository.findAllByMemberId(id)
                .last()
        findGenerationMember.updatePosition(
                Position.from(position),
                SubPosition.from(subPosition)
        )
    }

    fun updatePassword(loggedInMember: Member, newPassword: Password) {
        loggedInMember.updatePassword(newPassword)
    }

    @Transactional(readOnly = true)
    fun getByEmail(email: String): Member {
        return memberRepository.findByEmail(email)
                ?: throw NotFoundException.memberNotFound()
    }

    fun deleteByAdministrator(id: Long) {
        // 회원에 해당되는 기수회원 정보들 삭제
        generationMemberRepository.findAllByMemberId(id)
                .map { generationMemberRepository.deleteById(it.id) }

        memberRepository.deleteById(id)
    }
}
