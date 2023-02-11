package nexters.admin.repository

import io.kotest.matchers.types.shouldBeSameInstanceAs
import nexters.admin.ADMIN_USERNAME
import nexters.admin.domain.user.Password
import nexters.admin.domain.user.administrator.Administrator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class AdminCacheRepositoryTest(
        @Autowired private val adminRepository: AdministratorRepository,
) {
    private val adminCacheRepository = AdminCacheRepository(adminRepository)

    @BeforeEach
    fun setUp() {
        adminRepository.save(Administrator(ADMIN_USERNAME, Password("1234")))
    }

    @AfterEach
    fun tearDown() {
        adminRepository.deleteAll()
    }

    @Test
    fun `동일한 어드민 인스턴스를 매번 반환`() {
        val admin = adminCacheRepository.findByUsername(ADMIN_USERNAME)
        val sameAdmin = adminCacheRepository.findByUsername(ADMIN_USERNAME)

        admin shouldBeSameInstanceAs sameAdmin
    }
}
