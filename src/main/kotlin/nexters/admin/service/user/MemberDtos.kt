package nexters.admin.service.user

import nexters.admin.domain.generation_member.GenerationMember
import nexters.admin.domain.user.member.Member

data class FindProfileResponse(
        val name: String,
        val generation: Int,
        val position: String
) {
    companion object {
        fun of(member: Member, generationMember: GenerationMember?): FindProfileResponse {
            return FindProfileResponse(
                    member.name,
                    generationMember?.generation ?: 0,
                    generationMember?.position?.value ?: ""
            )
        }
    }
}
