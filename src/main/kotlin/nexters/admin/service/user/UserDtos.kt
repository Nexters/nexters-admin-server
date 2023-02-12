package nexters.admin.service.user

import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.user.member.Member

data class FindAllMembersResponse(
        val data: List<FindMemberResponse>,
)

data class FindMemberResponse(
        val id: Long,
        val name: String,
        val gender: String,
        val email: String,
        val phoneNumber: String,
        val generations: List<Int>,
        val position: String?,
        val subPosition: String?,
        val status: String,
        val isManager: Boolean,
) {
    companion object {
        fun of(member: Member, generationMembers: List<GenerationMember>): FindMemberResponse {
            return FindMemberResponse(
                    member.id,
                    member.name,
                    member.gender.value,
                    member.email,
                    member.phoneNumber,
                    generationMembers.map { it.generation },
                    generationMembers.last().position?.value ?: "",
                    generationMembers.last().subPosition?.value ?: "",
                    member.status.value,
                    generationMembers.last().isManager
            )
        }
    }
}
