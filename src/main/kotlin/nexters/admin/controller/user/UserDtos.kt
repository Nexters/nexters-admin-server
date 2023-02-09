package nexters.admin.controller.user

import nexters.admin.domain.user.Password
import javax.validation.constraints.Email
import javax.validation.constraints.Pattern

data class UpdatePasswordRequest(
        @field:Pattern(
                regexp = "^[a-zA-Z0-9!@#$%^*]{8,20}$",
                message = "비밀번호는 알파벳, 숫자, 특수문자(!,@,#,\\,$,%,^,*) 8~20 글자로 구성되어야 합니다.")
        val password: Password
)

data class TokenResponse(val data: String)

data class UpdateMemberRequest(
        val name: String,
        val gender: String,
        @field:Email
        val email: String,
        val phoneNumber: String,
        val generations: List<Int>,
)
