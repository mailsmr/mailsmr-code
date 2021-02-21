package io.mailsmr.common.infrastructure.repositories

import io.mailsmr.common.infrastructure.entities.JwtRefreshTokenGrant
import io.mailsmr.common.infrastructure.entities.User
import org.springframework.data.jpa.repository.JpaRepository

interface JwtRefreshTokenGrantRepository : JpaRepository<JwtRefreshTokenGrant, Long> {
    fun findByJti(jti: String): JwtRefreshTokenGrant?

    fun existsByJti(jti: String): Boolean

    fun deleteByJti(jti: String)

    fun findAllByUser(user: User): List<JwtRefreshTokenGrant>
}
