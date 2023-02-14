package nexters.admin.service.user

import nexters.admin.repository.AdminCacheRepository
import org.springframework.stereotype.Service

@Service
class AdminService(
        private val adminCacheRepository: AdminCacheRepository,
) {
    fun checkByUsername(username: String): Boolean {
        return adminCacheRepository.findByUsername(username) != null
    }
}
