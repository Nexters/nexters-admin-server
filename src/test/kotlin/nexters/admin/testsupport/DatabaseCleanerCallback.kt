package nexters.admin.testsupport

import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.test.context.junit.jupiter.SpringExtension

class DatabaseCleanerCallback : BeforeEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        SpringExtension.getApplicationContext(context)
                .getBean(DatabaseCleanser::class.java)
                .execute()
    }
}
