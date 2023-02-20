package nexters.admin.testsupport

import nexters.admin.repository.AdminCacheRepository
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import javax.persistence.Entity
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.Table
import javax.persistence.metamodel.EntityType

@Component
class DatabaseCleanser(
        @PersistenceContext private val entityManager: EntityManager,
        private val tableNames: MutableList<String> = mutableListOf(),
        @Autowired private val adminCacheRepository: AdminCacheRepository,
) : InitializingBean {

    override fun afterPropertiesSet() {
        val tableNames = entityManager.metamodel.entities
                .filter { e -> e.javaType.getAnnotation(Entity::class.java) != null }
                .map { e -> extractTableName(e).lowercase() }
        this.tableNames.addAll(tableNames)
    }

    private fun extractTableName(e: EntityType<*>): String {
        val tableAnnotation = e.javaType.getAnnotation(Table::class.java)
        return tableAnnotation?.name ?: e.name
    }

    @Transactional
    fun execute() {
        entityManager.flush()
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate()
        adminCacheRepository.deleteAll()
        tableNames.forEach {
            entityManager.createNativeQuery("TRUNCATE TABLE $it").executeUpdate()
            entityManager.createNativeQuery("ALTER TABLE $it ALTER COLUMN ID RESTART WITH 1").executeUpdate()
        }
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate()
    }
}
