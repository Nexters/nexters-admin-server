package nexters.admin.service.user

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nexters.admin.controller.user.CreateAdministratorRequest
import nexters.admin.exception.BadRequestException
import nexters.admin.repository.AdministratorRepository
import nexters.admin.testsupport.ADMIN_USERNAME
import nexters.admin.testsupport.ApplicationTest
import nexters.admin.testsupport.createNewAdmin
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ApplicationTest
class AdminServiceTest(
        @Autowired private val adminService: AdminService,
        @Autowired private val adminRepository: AdministratorRepository,
) {
    @Test
    fun `관리자 단건 생성`() {
        adminService.createAdministrator(CreateAdministratorRequest(ADMIN_USERNAME, "1234"))

        val findAdministrator = adminRepository.findByUsername(ADMIN_USERNAME)

        findAdministrator shouldNotBe null
        findAdministrator?.username shouldBe ADMIN_USERNAME
    }

    @Test
    fun `해당 관리자 계정이 이미 있을 경우 예외 반환`() {
        adminRepository.save(createNewAdmin())

        shouldThrow<BadRequestException> {
            adminService.createAdministrator(CreateAdministratorRequest(ADMIN_USERNAME, "1234"))
        }
    }

    @Test
    fun `아이디에 해당되는 어드민의 존재 여부 반환`() {
        adminRepository.save(createNewAdmin())

        adminService.checkByUsername(ADMIN_USERNAME) shouldBe true
        adminService.checkByUsername("not_admin") shouldBe false
    }
}
