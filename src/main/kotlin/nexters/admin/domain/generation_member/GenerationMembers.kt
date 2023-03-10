package nexters.admin.domain.generation_member

import nexters.admin.domain.user.member.EMAIL
import nexters.admin.domain.user.member.Member
import nexters.admin.exception.BadRequestException
import nexters.admin.support.utils.hasNoDuplicates
import nexters.admin.support.utils.validateCsvColumns

private const val POSITION = "position"
private const val SUB_POSITION = "sub_position"

val REQUIRED_KEYS = listOf(POSITION, SUB_POSITION)

// TODO: add tests
class GenerationMembers(
        private val values: Map<Long, GenerationMember>,
) {
    companion object {
        fun of(
                generation: Int,
                generationMembers: Map<String, List<String>>,
                savedMembers: List<Member>,
        ): GenerationMembers {
            validateCsvColumns(generationMembers, REQUIRED_KEYS)
            if (!hasNoDuplicates(generationMembers, EMAIL)) {
                throw BadRequestException.duplicateEmail()
            }
            val emailToMemberIdMap = mapMemberIdByEmail(savedMembers)
            val values = mutableMapOf<Long, GenerationMember>()
            for (idx in 0 until generationMembers[EMAIL]!!.size) {
                val email = generationMembers[EMAIL]?.get(idx) ?: throw BadRequestException.missingInfo("회원의 이메일")
                val memberId = emailToMemberIdMap[email] ?: throw RuntimeException("wrong implementation")
                values[memberId] = (GenerationMember(
                        memberId = memberId,
                        generation = generation,
                        position = generationMembers[POSITION]?.get(idx)?.let { Position.from(it) }
                                ?: throw BadRequestException.missingInfo("회원의 직군"),
                        subPosition = generationMembers[SUB_POSITION]?.get(idx)?.let { SubPosition.from(it) }
                                ?: throw BadRequestException.missingInfo("회원의 세부직군"),
                ))
            }
            return GenerationMembers(values.toMap())
        }

        private fun mapMemberIdByEmail(savedMembers: List<Member>): Map<String, Long> {
            val emailToMemberIdMap = mutableMapOf<String, Long>()
            for (member in savedMembers) {
                member.id.let {
                    emailToMemberIdMap[member.email] = it
                }
            }
            return emailToMemberIdMap
        }
    }

    fun findAllByMemberIdsNotIn(memberIds: List<Long>): List<GenerationMember> {
        val members = values.values.toMutableList()
        for (memberId in memberIds) {
            values[memberId]?.let { members.remove(it) }
        }
        return members
    }

    fun updateGenerationMembersWithMatchingMemberId(
            currentGenerationMembers: List<GenerationMember>,
    ) {
        for (currentGenerationMember in currentGenerationMembers) {
            values[currentGenerationMember.memberId]?.let {
                currentGenerationMember.updatePosition(it.position, it.subPosition)
            }
        }
    }
}
