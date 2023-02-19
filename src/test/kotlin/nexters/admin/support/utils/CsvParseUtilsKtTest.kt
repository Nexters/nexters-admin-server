package nexters.admin.support.utils

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.io.FileInputStream
import java.net.URL

class CsvParseUtilsKtTest {

    @Test
    fun `csv 파일로부터 값 추출하여 Map 반환`() {
        val url: URL? = this::class.java.classLoader.getResource("testdata.csv")
        val multipartFile = MockMultipartFile("testdata.csv", FileInputStream(File(url!!.path)))
        val actual = parseCsvFileToMap(multipartFile)
        val expected = mutableMapOf(
                "name" to mutableListOf("정진우", "김민수"),
                "gender" to mutableListOf("남자", "남자"),
                "email" to mutableListOf("jinwoo@gmail.com", "ming@gmail.com"),
                "phone_number" to mutableListOf("01012345678", "01012345679"),
                "position" to mutableListOf("개발자", "운영진"),
                "sub_position" to mutableListOf("프론트엔드", "CTO"),
                "status" to mutableListOf("미이수", "수료")
        )
        actual shouldBe expected
    }
}
