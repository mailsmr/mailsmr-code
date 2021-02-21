package io.mailsmr.domain

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.InvalidKeyException
import java.time.Clock
import java.time.Duration
import java.util.*
import javax.servlet.http.HttpServletRequest

@Service
class JwtTokenFactory(
    private val clock: Clock
) {
    @Value("\${jwt.secret}")
    private lateinit var secret: String

    companion object {
        val JWT_ACCESS_TOKEN_VALIDITY_DURATION: Duration = Duration.ofMinutes(30)
        val JWT_REFRESH_TOKEN_VALIDITY_DURATION: Duration = Duration.ofDays(30)
    }

    fun fromRequest(request: HttpServletRequest): JwtAccessToken? {
        val authorizationHeader = request.getHeader("Authorization") ?: return null
        return fromAuthorizationHeader(authorizationHeader)
    }

    fun fromAuthorizationHeader(authorizationHeader: String): JwtAccessToken {
        if (JwtToken.isJwtAuthorizationHeader(authorizationHeader)) {
            return JwtAccessToken(authorizationHeader.substring(7), secret, clock)
        }
        throw InvalidKeyException("The provided jtw token in the Authorization header is invalid.")
    }

    fun fromRefreshTokenString(tokenString: String) = JwtRefreshToken(tokenString, secret, clock)

    fun fromAccessTokenString(tokenString: String) = JwtAccessToken(tokenString, secret, clock)

    fun generateAccessToken(
        username: String,
        encryptedMasterPassword: String,
        expirationDuration: Duration = JWT_ACCESS_TOKEN_VALIDITY_DURATION
    ): JwtAccessToken {
        val claims: MutableMap<String, Any> = HashMap()
        claims[JwtToken.ENCRYPTED_MASTER_PASSWORD_CLAIM] = encryptedMasterPassword
        return generateAccessToken(claims, username, expirationDuration)
    }

    private fun generateAccessToken(
        claims: Map<String, Any>,
        subject: String,
        expirationDuration: Duration
    ): JwtAccessToken {
        val jwtTokenString: String = Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(Date(clock.millis()))
            .setExpiration(Date(clock.millis() + expirationDuration.toMillis()))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()

        return JwtAccessToken(jwtTokenString, secret, clock)
    }

    fun generateRefreshToken(
        username: String,
        expirationDuration: Duration = JWT_REFRESH_TOKEN_VALIDITY_DURATION
    ): JwtRefreshToken {
        val claims: Map<String, Any> = HashMap()
        return generateRefreshToken(claims, username, expirationDuration)
    }

    private fun generateRefreshToken(
        claims: Map<String, Any>,
        subject: String,
        expirationDuration: Duration
    ): JwtRefreshToken {
        val jwtTokenString: String = Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(Date(clock.millis()))
            .setExpiration(Date(clock.millis() + expirationDuration.toMillis()))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()

        return JwtRefreshToken(jwtTokenString, secret, clock)
    }
}
