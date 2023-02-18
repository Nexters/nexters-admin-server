package nexters.admin.testsupport

import nexters.admin.repository.QrCodeRepository
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DatabaseCleanser::class, QrCodeRepository::class)
@ExtendWith(DatabaseCleanerCallback::class)
annotation class RepositoryTest()
