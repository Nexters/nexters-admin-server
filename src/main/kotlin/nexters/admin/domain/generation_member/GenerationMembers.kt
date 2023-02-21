package nexters.admin.domain.generation_member

import nexters.admin.domain.user.member.EMAIL
import nexters.admin.domain.user.member.Member
import nexters.admin.exception.BadRequestException

private const val POSITION = "position"
private const val SUB_POSITION = "sub_position"

val REQUIRED_KEYS = listOf(POSITION, SUB_POSITION)

// TODO: add tests
class GenerationMembers(
        private val values: Map<Long, GenerationMember>,
) {
    companion object {
        fun of(
                generation: Long,
                generationMembers: Map<String, List<String>>,
                savedMembers: List<Member>,
        ): GenerationMembers {
            validate(generationMembers)
            val emailToMemberIdMap = mapMemberIdByEmail(savedMembers)
            val values = mutableMapOf<Long, GenerationMember>()
            for (idx in 0 until generationMembers[EMAIL]!!.size) {
                val email = generationMembers[EMAIL]?.get(idx) ?: throw BadRequestException.missingInfo("회원의 이메일")
                val memberId = emailToMemberIdMap[email] ?: throw RuntimeException("wrong implementation")
                values[memberId] = (GenerationMember(
                        memberId = memberId,
                        generation = generation.toInt(),
                        position = generationMembers[POSITION]?.get(idx)?.let { Position.from(it) }
                                ?: throw BadRequestException.missingInfo("회원의 전화번호"),
                        subPosition = generationMembers[SUB_POSITION]?.get(idx)?.let { SubPosition.from(it) }
                                ?: throw BadRequestException.missingInfo("회원의 전화번호"),
                ))
            }
            return GenerationMembers(values.toMap())
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

    fun updateGenerationMembersWithMatchingEmail(
            members: List<Member>,
            currentGenerationMembers: List<GenerationMember>,
    ) {
        val memberIdToGenerationMemberMap = mapByMemberId(currentGenerationMembers)
        for (member in members) {
            val currentGenerationMember = memberIdToGenerationMemberMap[member.id]
            values[member.id]?.let {
                currentGenerationMember?.updatePosition(it.position, it.subPosition)
            }
        }
    }

    private fun mapByMemberId(curGenMembers: List<GenerationMember>): MutableMap<Long, GenerationMember> {
        val curGenMemberMap = mutableMapOf<Long, GenerationMember>()
        for (genMember in curGenMembers) {
            genMember.memberId.let { curGenMemberMap[it] = genMember }
        }
        return curGenMemberMap
    }
}
