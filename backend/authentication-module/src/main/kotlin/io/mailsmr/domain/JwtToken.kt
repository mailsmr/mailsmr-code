package io.mailsmr.domain

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import java.time.Clock
import java.util.*
import java.util.function.Function

open class JwtToken protected constructor(
    private val jwtTokenString: String,
    private val jwtTokenSecret: String,
    private val clock: Clock
) {
    companion object {
        fun isJwtAuthorizationHeader(requestTokenHeader: String): Boolean = requestTokenHeader.startsWith("Bearer ")

        const val ENCRYPTED_MASTER_PASSWORD_CLAIM = "encryptedMasterPassword"
    }

    fun getIssuedAtDate(): Date = getClaimFromToken { obj: Claims -> obj.issuedAt }

    fun getExpirationDate(): Date = getClaimFromToken { obj: Claims -> obj.expiration }

    fun isExpired(): Boolean = clock.instant().isAfter(getExpirationDate().toInstant())

    fun getUsername(): String? {
        return getClaimFromToken { obj: Claims -> obj.subject }
    }

    fun isUsernameSet(): Boolean = getUsername() != null

    protected open fun getJti(): String? = getAllClaims()["jti"] as String?

    protected fun isJtiSet() = getJti() != null

    override fun toString(): String = jwtTokenString

    fun isValid(): Boolean = isViable() && !isExpired()

    open fun isViable(): Boolean {
        return try {
            getJwtParser().setSigningKey(jwtTokenSecret).parse(jwtTokenString)
            true
        } catch (e: ExpiredJwtException) {
            true
        } catch (e: Exception) {
            false
        }
    }

    protected fun <T> getClaimFromToken(claimsResolver: Function<Claims, T>): T {
        val claims: Claims = getAllClaims()
        return claimsResolver.apply(claims)
    }

    protected fun getAllClaims(): Claims {
        return try {
            getJwtParser().setSigningKey(jwtTokenSecret).parseClaimsJws(jwtTokenString).body
        } catch (exception: ExpiredJwtException) {
            exception.claims
        }
    }

    private fun getJwtParser() = Jwts.parser().setClock { Date(clock.millis()) }

}
