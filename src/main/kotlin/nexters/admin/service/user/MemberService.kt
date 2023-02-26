package nexters.admin.service.user

import nexters.admin.controller.generation.CreateGenerationRequest
import nexters.admin.controller.user.CreateMemberRequest
import nexters.admin.controller.user.UpdateMemberRequest
import nexters.admin.domain.attendance.Attendance
import nexters.admin.domain.attendance.AttendanceStatus
import nexters.admin.domain.generation.GenerationStatus.BEFORE_ACTIVITY
import nexters.admin.domain.generation.GenerationStatus.DURING_ACTIVITY
import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.generation_member.GenerationMembers
import nexters.admin.domain.generation_member.Position
import nexters.admin.domain.generation_member.SubPosition
import nexters.admin.domain.user.Password
import nexters.admin.domain.user.member.Gender
import nexters.admin.domain.user.member.Member
import nexters.admin.domain.user.member.MemberStatus
import nexters.admin.domain.user.member.Members
import nexters.admin.exception.NotFoundException
import nexters.admin.repository.AttendanceRepository
import nexters.admin.repository.GenerationMemberRepository
import nexters.admin.repository.GenerationRepository
import nexters.admin.repository.MemberRepository
import nexters.admin.repository.SessionRepository
import nexters.admin.service.generation.GenerationService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class MemberService(
        private val generationService: GenerationService,
        private val memberRepository: MemberRepository,
        private val generationMemberRepository: GenerationMemberRepository,
        private val generationRepository: GenerationRepository,
        private val sessionRepository: SessionRepository,
        private val attendanceRepository: AttendanceRepository,
) {
    fun createMemberByAdministrator(request: CreateMemberRequest): Long {
        val savedMember = memberRepository.save(
                Member.of(
                        request.name,
                        request.email,
                        request.gender,
                        request.phoneNumber,
                        request.status
                )
        )

        val currentGeneration = generationRepository.findAll()
                .filter { it.status in listOf(BEFORE_ACTIVITY, DURING_ACTIVITY) }
                .maxByOrNull { it.generation }
                ?.generation

        currentGeneration?.let {
            if (request.generations.contains(it)) {
                val generationMember = createCurrentGenerationMember(savedMember, request, currentGeneration)
                createAttendances(generationMember)
            }
        }
        createBeforeGenerationMembers(request, savedMember)

        return savedMember.id
    }

    private fun createCurrentGenerationMember(
            savedMember: Member,
            request: CreateMemberRequest,
            currentGeneration: Int): GenerationMember {
        val generationMember = generationMemberRepository.save(
                GenerationMember(
                        memberId = savedMember.id,
                        generation = currentGeneration,
                        position = Position.from(request.position),
                        subPosition = SubPosition.from(request.subPosition),
                )
        )
        request.generations.remove(currentGeneration)
        return generationMember
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
                            )
                    )
                }
    }

    fun createGenerationMembers(generation: Int, memberMap: Map<String, List<String>>) {
        generationRepository.findByGeneration(generation)
                ?: generationService.createGeneration(CreateGenerationRequest(generation))

        val members = Members.of(memberMap)
        val existingMembers = memberRepository.findAllByEmailIn(members.getEmails())
        members.updateMembersWithMatchingEmail(existingMembers)
        val savedMemberEmails = existingMembers.map { it.email }
        val savedMembers = memberRepository.saveAll(members.findAllByEmailNotIn(savedMemberEmails)).toMutableList()
        savedMembers.addAll(existingMembers)

        val newGenerationMembers = createGenerationMembers(generation, memberMap, savedMembers)
        newGenerationMembers.forEach {
            createAttendances(it)
        }
    }

    private fun createGenerationMembers(generation: Int, memberMap: Map<String, List<String>>, savedMembers: List<Member>): List<GenerationMember> {
        val generationMembers = GenerationMembers.of(generation, memberMap, savedMembers)
        val existingGenerationMembers = updateExistingGenerationMembers(savedMembers, generationMembers)
        return saveNewGenerationMembers(generationMembers, existingGenerationMembers)
    }

    private fun updateExistingGenerationMembers(savedMembers: List<Member>, generationMembers: GenerationMembers): List<GenerationMember> {
        val existingGenerationMembers = generationMemberRepository.findAllByMemberIdIn(savedMembers.map { it.id })
        generationMembers.updateGenerationMembersWithMatchingMemberId(existingGenerationMembers)
        return existingGenerationMembers
    }

    private fun saveNewGenerationMembers(generationMembers: GenerationMembers, existingGenerationMembers: List<GenerationMember>): List<GenerationMember> {
        val newGenerationMembers = generationMembers.findAllByMemberIdsNotIn(existingGenerationMembers.map { it.memberId })
        generationMemberRepository.saveAll(newGenerationMembers)
        return newGenerationMembers
    }

    private fun createAttendances(generationMember: GenerationMember) {
        val sessions = sessionRepository.findAllByGeneration(generationMember.generation)
        val attendances = mutableListOf<Attendance>()
        sessions.forEach { session ->
            attendances.add(Attendance(
                    generationMemberId = generationMember.id,
                    sessionId = session.id,
                    attendanceStatus = AttendanceStatus.PENDING,
            ))
        }
        attendanceRepository.saveAll(attendances)
    }

    @Transactional(readOnly = true)
    fun findAllByAdministrator(): FindAllMembersResponse {
        val members = memberRepository.findAll()
        val generationMembers = generationMemberRepository.findAll()
                .groupBy { it.memberId }

        return findAllGenerationMembersByMemberId(generationMembers, members)
    }

    private fun findAllGenerationMembersByMemberId(
            generationMembers: Map<Long, List<GenerationMember>>,
            members: List<Member>,
    ): FindAllMembersResponse {
        val findAllMembers: MutableList<FindMemberResponse> = mutableListOf()
        var currentMemberIndex = 0

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

    fun updateStatusByAdministrator(memberId: Long, status: String) {
        val findMember = memberRepository.findByIdOrNull(memberId)
                ?: throw NotFoundException.memberNotFound()
        findMember.update(status = MemberStatus.from(status))
    }

    fun updatePositionByAdministrator(memberId: Long, position: String?, subPosition: String?) {
        memberRepository.findByIdOrNull(memberId)
                ?: throw NotFoundException.memberNotFound()
        val findGenerationMember = generationMemberRepository.findAllByMemberId(memberId)
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
        memberRepository.findByIdOrNull(id)
                ?: throw NotFoundException.memberNotFound()
        generationMemberRepository.findAllByMemberId(id)
                .map { generationMemberRepository.deleteById(it.id) }
        memberRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getProfile(loggedInMember: Member): FindProfileResponse {
        val generationMember = generationMemberRepository.findTopByMemberIdOrderByGenerationDesc(loggedInMember.id)
        return FindProfileResponse.of(loggedInMember, generationMember)
    }
}
