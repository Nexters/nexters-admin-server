package nexters.admin.service.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
        @Value("\${security.jwt.token.secret-key}") private val secretKey: SecretKey,
        @Value("\${security.jwt.token.validity}") private val expirationTime: Long,
) {
    fun generateToken(payload: String): String {
        val claims: Claims = Jwts.claims().setSubject(payload)
        val now = Date()
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(Date(now.time + expirationTime))
                .signWith(secretKey)
                .compact()
    }
}
