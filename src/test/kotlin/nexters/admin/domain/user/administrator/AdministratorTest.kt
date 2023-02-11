package nexters.admin.domain.user.administrator

import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import nexters.admin.createNewAdmin
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class AdministratorTest {

    @Test
    fun `비밀번호 일치 여부 반환`() {
        val password = "abcd1234"
        val admin = createNewAdmin(password = password)

        admin.isSamePassword(password) shouldBe true
    }

    @Test
    fun `현재 시각으로 마지막 접속 시점 수정`() {
        val prevAccessTime = LocalDateTime.now().minus(3, ChronoUnit.SECONDS)
        val admin = createNewAdmin(lastAccessTime = prevAccessTime)

        admin.updateLastAccessTime()
        admin.lastAccessTime shouldBeGreaterThan prevAccessTime
    }
}
