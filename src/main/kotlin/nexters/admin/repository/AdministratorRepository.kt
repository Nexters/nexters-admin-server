package nexters.admin.repository

import nexters.admin.domain.user.administrator.Administrator
import org.springframework.data.jpa.repository.JpaRepository

interface AdministratorRepository : JpaRepository<Administrator, Long> {
    fun findByUsername(username: String): Administrator?
}
