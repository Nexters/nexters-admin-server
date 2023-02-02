package nexters.admin.service.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import nexters.admin.common.exception.UnauthenticatedException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
        @Value("\${security.jwt.token.secret-key}") val secretKey: String,
        @Value("\${security.jwt.token.validity}") val expirationTime: Long,
) {
    private val signingKey: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))

    fun generateToken(payload: String): String {
        val claims: Claims = Jwts.claims().setSubject(payload)
        val now = Date()
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(Date(now.time + expirationTime))
                .signWith(signingKey)
                .compact()
    }

    fun getPayload(token: String): String {
        try {
            return extractPayload(token)
        } catch (e: JwtException) {
            throw UnauthenticatedException.loginNeeded()
        } catch (e: IllegalArgumentException) {
            throw UnauthenticatedException.loginNeeded()
        }
    }

    private fun extractPayload(token: String) = Jwts.parserBuilder()
            .setSigningKey(signingKey.encoded)
            .build()
            .parseClaimsJws(token)
            .body
            .subject
}
