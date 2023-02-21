package nexters.admin.support.utils

import io.kotest.matchers.ints.shouldBeExactly
import org.junit.jupiter.api.Test

class RandomUtilsKtTest {

    @Test
    fun `주어진 길이만큼의 알파벳(대소문자)과 숫자로만 이루어진 임의의 문자열을 반환`() {
        val length = 6
        val actual = randomStringLengthOf(length)

        actual.length shouldBeExactly length
        actual matches Regex("^[0-9a-zA-Z]+$")
    }
}
