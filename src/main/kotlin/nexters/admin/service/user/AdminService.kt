package nexters.admin.service.user

import nexters.admin.controller.user.CreateAdministratorRequest
import nexters.admin.domain.user.Password
import nexters.admin.domain.user.administrator.Administrator
import nexters.admin.exception.BadRequestException
import nexters.admin.repository.AdminCacheRepository
import nexters.admin.repository.AdministratorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AdminService(
        private val adminCacheRepository: AdminCacheRepository,
        private val administratorRepository: AdministratorRepository,
) {
    fun createAdministrator(request: CreateAdministratorRequest) {
        adminCacheRepository.findByUsername(request.username)
                ?.run { throw BadRequestException.alreadyExistsAdministrator() }

        val administrator = Administrator(request.username, Password(request.password))
        administratorRepository.save(administrator)
    }

    @Transactional(readOnly = true)
    fun checkByUsername(username: String): Boolean {
        return adminCacheRepository.findByUsername(username) != null
    }
}
