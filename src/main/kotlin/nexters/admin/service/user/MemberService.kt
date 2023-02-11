package nexters.admin.service.user

import nexters.admin.controller.user.CreateMemberRequest
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

private const val DEFAULT_PASSWORD_LENGTH = 4

@Transactional
@Service
class MemberService(
        private val memberRepository: MemberRepository,
        private val generationMemberRepository: GenerationMemberRepository,
) {
    fun createMemberByAdministrator(request: CreateMemberRequest): Long {
        val savedMember = memberRepository.save(
                Member(
                        request.name,
                        Password(createDefaultPassword(request.phoneNumber)),
                        request.email,
                        Gender.from(request.gender),
                        request.phoneNumber,
                        MemberStatus.from(request.status)
                )
        )
        createCurrentGenerationMember(savedMember, request)
        createBeforeGenerationMembers(request, savedMember)

        return savedMember.id
    }

    private fun createCurrentGenerationMember(savedMember: Member, request: CreateMemberRequest) {
        // 이전 기수 회원 저장 (이전 기수의 직군은 최신직군으로 저장, 이전 기수의 점수는 null 로 저장)
        generationMemberRepository.save(
                GenerationMember(
                        memberId = savedMember.id,
                        generation = request.generations.last(),
                        position = Position.from(request.position),
                        subPosition = SubPosition.from(request.subPosition),
                        isManager = request.isManager
                )
        )
        request.generations.removeLast()
    }

    private fun createBeforeGenerationMembers(request: CreateMemberRequest, savedMember: Member) {
        request.generations
                .forEach {
                    generationMemberRepository.save(
                            GenerationMember(
                                    memberId = savedMember.id,
                                    generation = it,
                                    position = Position.from(request.position),
                                    subPosition = SubPosition.from(request.subPosition),
                                    score = null,
                                    isManager = request.isManager
                            )
                    )
                }
    }

    private fun createDefaultPassword(phoneNumber: String): String {
        return phoneNumber.substring(phoneNumber.length - DEFAULT_PASSWORD_LENGTH, phoneNumber.length)
    }

    @Transactional(readOnly = true)
    fun findAllByAdministrator(): FindAllMembersResponse {
        val members = memberRepository.findAll()
        val generationMembers = generationMemberRepository.findAll()
                .groupBy { it.memberId }

        return findAllGenerationMembersByMemberId(generationMembers, members)
    }

    private fun findAllGenerationMembersByMemberId(
            generationMembers: Map<Long?, List<GenerationMember>>,
            members: List<Member>,
    ): FindAllMembersResponse {
        val findAllMembers: MutableList<FindMemberResponse> = mutableListOf()
        var currentMemberIndex = 0

        // 만약 현재기수의 직군 정보가 없으면 일단은 가장 최근 기수의 직군 정보를 반환하도록 구현
        generationMembers.forEach {
            findAllMembers.add(FindMemberResponse.of(members[currentMemberIndex++], it.value))
        }

        return FindAllMembersResponse(findAllMembers)
    }

    fun updateMemberByAdministrator(id: Long, updateMemberRequest: UpdateMemberRequest) {
        val findMember = memberRepository.findByIdOrNull(id)
                ?: throw NotFoundException.memberNotFound()
        updateMember(findMember, updateMemberRequest)
        updateGenerateMember(updateMemberRequest, findMember)
    }

    private fun updateMember(findMember: Member, updateMemberRequest: UpdateMemberRequest) {
        findMember.update(
                updateMemberRequest.name,
                Gender.from(updateMemberRequest.gender),
                updateMemberRequest.phoneNumber
        )
    }

    private fun updateGenerateMember(updateMemberRequest: UpdateMemberRequest, findMember: Member) {
        val findGenerationMembers = generationMemberRepository.findAllByMemberId(findMember.id)
        addRequestedGenerationMembers(updateMemberRequest, findGenerationMembers, findMember)
        deleteUnrequestedGenerationMembers(findGenerationMembers, updateMemberRequest)
    }

    private fun addRequestedGenerationMembers(
            updateMemberRequest: UpdateMemberRequest,
            findGenerationMembers: List<GenerationMember>,
            findMember: Member,
    ) {
        updateMemberRequest.generations
                .filterNot { checkIfRequestedGenerationsExists(findGenerationMembers, it) }
                .forEach {
                    generationMemberRepository.save(
                            GenerationMember(
                                    memberId = findMember.id,
                                    generation = it,
                                    position = null,
                                    subPosition = null
                            )
                    )
                }
    }

    private fun checkIfRequestedGenerationsExists(findGenerationMembers: List<GenerationMember>, it1: Int) =
            findGenerationMembers.map { it.generation }.contains(it1)

    private fun deleteUnrequestedGenerationMembers(
            findGenerationMembers: List<GenerationMember>,
            updateMemberRequest: UpdateMemberRequest,
    ) {
        findGenerationMembers
                .filterNot { checkIfRequestedGenerations(updateMemberRequest, it) }
                .forEach { generationMemberRepository.deleteById(it.id) }
    }

    private fun checkIfRequestedGenerations(updateMemberRequest: UpdateMemberRequest, it: GenerationMember) =
            updateMemberRequest.generations.contains(it.generation)

    fun updateStatusByAdministrator(id: Long, status: String) {
        val findMember = memberRepository.findByIdOrNull(id)
                ?: throw NotFoundException.memberNotFound()
        findMember.updateStatus(MemberStatus.from(status))
    }

    fun updatePositionByAdministrator(id: Long, position: String?, subPosition: String?) {
        val findGenerationMember = generationMemberRepository.findAllByMemberId(id)
                .last()
        findGenerationMember.updatePosition(
                Position.from(position),
                SubPosition.from(subPosition)
        )
    }

    fun updatePassword(loggedInMember: Member, newPassword: String) {
        loggedInMember.updatePassword(Password(newPassword))
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
