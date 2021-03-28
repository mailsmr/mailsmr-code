package io.mailsmr.common.infrastructure.repositories

import io.mailsmr.common.infrastructure.entities.JwtRefreshTokenGrant
import io.mailsmr.common.infrastructure.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface JwtRefreshTokenGrantRepository : JpaRepository<JwtRefreshTokenGrant, Long> {
    fun findByJti(jti: String): JwtRefreshTokenGrant?

    fun existsByJti(jti: String): Boolean

    fun deleteByJti(jti: String)

    fun findAllByUser(user: User): List<JwtRefreshTokenGrant>

    @Modifying
    @Transactional
    @Query("DELETE FROM jwt_refresh_token_grants g WHERE g.expiration_date_time < NOW()", nativeQuery = true)
    fun deleteAllExpired()
}
