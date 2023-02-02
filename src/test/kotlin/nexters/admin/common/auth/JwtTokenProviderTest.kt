package nexters.admin.common.auth

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nexters.admin.common.exception.UnauthenticatedException
import org.junit.jupiter.api.Test

private const val VALID_SECRET_KEY = "testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest"
private const val INVALID_SECRET_KEY = "wrongtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest"
private const val EXPIRATION_TIME: Long = 3600000

class JwtTokenProviderTest {

    @Test
    fun `토큰 발급 및 해석`() {
        val tokenProvider = JwtTokenProvider(VALID_SECRET_KEY, EXPIRATION_TIME)
        val payload = "jwjeong@gmail.com"

        val token = tokenProvider.generateToken(payload)
        val extractedPayload = tokenProvider.getPayload(token)

        payload shouldBe extractedPayload
    }

    @Test
    fun `다른 키로 발급된 토큰 해석`() {
        val payload = "jwjeong@gmail.com"
        val invalidProvider = JwtTokenProvider(INVALID_SECRET_KEY, EXPIRATION_TIME)
        val token = invalidProvider.generateToken(payload)

        val validProvider = JwtTokenProvider(VALID_SECRET_KEY, EXPIRATION_TIME)
        shouldThrow<UnauthenticatedException> {
            validProvider.getPayload(token)
        }
    }
}
