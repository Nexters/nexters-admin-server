package nexters.admin.service.user

import io.kotest.matchers.shouldBe
import nexters.admin.testsupport.ADMIN_USERNAME
import nexters.admin.domain.user.Password
import nexters.admin.domain.user.administrator.Administrator
import nexters.admin.repository.AdministratorRepository
import nexters.admin.testsupport.ApplicationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ApplicationTest
class AdminServiceTest(
        @Autowired private val adminService: AdminService,
        @Autowired private val adminRepository: AdministratorRepository,
) {
    @Test
    fun `아이디에 해당되는 어드민의 존재 여부 반환`() {
        adminRepository.save(Administrator(ADMIN_USERNAME, Password("1234")))

        adminService.checkByUsername(ADMIN_USERNAME) shouldBe true
        adminService.checkByUsername("not_admin") shouldBe false
    }
}
