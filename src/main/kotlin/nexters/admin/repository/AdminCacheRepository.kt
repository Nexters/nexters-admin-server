package nexters.admin.repository

import nexters.admin.domain.user.administrator.Administrator
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class AdminCacheRepository(
        private val adminRepository: AdministratorRepository,
) {
    private var cacheRepository: ConcurrentHashMap<String, Administrator> = ConcurrentHashMap()

    fun findByUsername(username: String): Administrator? {
        if (!cacheRepository.containsKey(username)) {
            updateCache(username)
        }
        return cacheRepository[username]
    }

    private fun updateCache(username: String) {
        adminRepository.findByUsername(username)
                ?.let { cacheRepository[username] = it }
    }

    fun deleteAll() {
        cacheRepository = ConcurrentHashMap()
    }
}
