package nexters.admin.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import nexters.admin.service.user.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Tag(name = "Administrators", description = "관리자")
@RequestMapping("/api/admin")
@RestController
class AdministratorController(
        private val adminService: AdminService,
) {
    @Operation(summary = "[관리자 페이지] 관리자 단건 생성")
    // TODO: DB에 임시 데이터 삽입 시 @SecurityRequirement(name = "JWT") 추가
    @PostMapping
    fun createAdministrator(@RequestBody @Valid request: CreateAdministratorRequest): ResponseEntity<Void> {
        adminService.createAdministrator(request)
        return ResponseEntity.ok().build()
    }
}
