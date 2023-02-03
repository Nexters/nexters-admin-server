package nexters.admin.common.auth

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nexters.admin.common.exception.UnauthenticatedException
import org.junit.jupiter.api.Test

private const val VALID_SECRET_KEY = "testtesttesttesttesttesttesttest"
private const val INVALID_SECRET_KEY = "wrongtesttesttesttesttesttesttest"
private const val EXPIRATION_TIME: Long = 3600000
private const val PAYLOAD = "jwjeong@gmail.com"

class JwtTokenProviderTest {

    @Test
    fun `토큰 발급 및 해석`() {
        val tokenProvider = JwtTokenProvider(VALID_SECRET_KEY, EXPIRATION_TIME)

        val token = tokenProvider.generateToken(PAYLOAD)
        val extractedPayload = tokenProvider.getPayload(token)

        extractedPayload shouldBe PAYLOAD
    }

    @Test
    fun `다른 키로 발급된 토큰 해석시 실패`() {
        val invalidProvider = JwtTokenProvider(INVALID_SECRET_KEY, EXPIRATION_TIME)
        val token = invalidProvider.generateToken(PAYLOAD)

        val validProvider = JwtTokenProvider(VALID_SECRET_KEY, EXPIRATION_TIME)
        shouldThrow<UnauthenticatedException> {
            validProvider.getPayload(token)
        }
    }
}
