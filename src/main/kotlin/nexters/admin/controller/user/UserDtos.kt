package nexters.admin.controller.user

import javax.validation.constraints.Email
import javax.validation.constraints.Size

data class CreateMemberRequest(
        val name: String,
        val gender: String,
        @field:Email(message = "잘못된 이메일 형식")
        val email: String,
        @field:Size(min = 10, max = 20, message = "휴대폰 번호는 `-` 없이 10자 이상 필요")
        val phoneNumber: String,
        val generations: MutableList<Int>,
        val position: String?,
        val subPosition: String?,
        val status: String,
)

data class CreateAdministratorRequest(
        val username: String,
        val password: String,
)

data class UpdatePasswordRequest(
        @field:Size(min = 8, max = 20, message = "비밀번호는 8~20 글자로 구성 필요")
        val password: String,
)

data class UpdateMemberRequest(
        val name: String,
        val gender: String,
        @field:Email(message = "잘못된 이메일 형식")
        val email: String,
        val phoneNumber: String,
        val generations: List<Int>,
)

data class UpdateMemberStatusRequest(
        val status: String,
)

data class UpdateMemberPositionRequest(
        val position: String?,
        val subPosition: String?,
)
