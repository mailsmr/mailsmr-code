package io.mailsmr.application

import io.mailsmr.common.infrastructure.entities.JwtRefreshTokenGrant
import io.mailsmr.common.infrastructure.entities.User
import io.mailsmr.common.infrastructure.repositories.JwtRefreshTokenGrantRepository
import io.mailsmr.domain.JwtRefreshToken
import io.mailsmr.domain.errors.ExpiredOrRevokedTokenException
import io.mailsmr.domain.errors.InvalidTokenException
import mu.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime
import java.util.stream.Collectors

@Service
class JwtRefreshTokenGrantService(
    private val jwtRefreshTokenGrantRepository: JwtRefreshTokenGrantRepository,
    private val clock: Clock
) {
    companion object {
        const val EXPIRED_GRANTS_DELETION_INITIAL_DELAY_IN_MILLIS: Long = 5 * 60 * 1000 // 5 minutes
        const val EXPIRED_GRANTS_DELETION_INTERVAL_IN_MILLIS: Long = 57 * 60 * 1000 // 57 minutes
    }

    private val logger = KotlinLogging.logger {}

    fun isRefreshTokenGranted(refreshToken: JwtRefreshToken): Boolean {
        if (refreshToken.isViable()) {
            val exists: Boolean = jwtRefreshTokenGrantRepository.existsByJti(refreshToken.getJti()!!)
            return if (exists && !refreshToken.isExpired()) {
                true
            } else {
                revokeRefreshToken(refreshToken)
                false
            }
        } else {
            throw InvalidTokenException()
        }
    }

    fun revokeRefreshToken(refreshToken: JwtRefreshToken) {
        if (refreshToken.isViable()) {
            jwtRefreshTokenGrantRepository.deleteByJti(refreshToken.getJti()!!)
            jwtRefreshTokenGrantRepository.flush()
        } else {
            throw InvalidTokenException()
        }
    }

    fun grantRefreshToken(refreshToken: JwtRefreshToken, user: User) {
        if (refreshToken.isViable()) {
            if (refreshToken.isExpired()) throw ExpiredOrRevokedTokenException()

            val refreshTokenGrant = JwtRefreshTokenGrant(
                jti = refreshToken.getJti()!!,
                expirationDateTime = refreshToken
                    .getExpirationDate()
                    .toInstant()
                    .atZone(clock.zone)
                    .toLocalDateTime(),
                creationDateTime = LocalDateTime.now(clock),
                user = user
            )

            try {
                jwtRefreshTokenGrantRepository.saveAndFlush(refreshTokenGrant)
            } catch (e: DataIntegrityViolationException){
                // stub - grant already exists in db
            }
        } else {
            throw InvalidTokenException()
        }
    }

    fun getGrantedRefreshTokensForUser(user: User): List<JwtRefreshTokenGrant> {
        return jwtRefreshTokenGrantRepository.findAllByUser(user).parallelStream()
            .filter { token -> token.expirationDateTime.isAfter(LocalDateTime.now(clock)) }
            .collect(Collectors.toList())
    }


    @Async
    @Scheduled(
        fixedRate = EXPIRED_GRANTS_DELETION_INTERVAL_IN_MILLIS,
        initialDelay = EXPIRED_GRANTS_DELETION_INITIAL_DELAY_IN_MILLIS
    )
    fun deleteExpiredRefreshTokensFromDatabase() {
        logger.info("Expired refresh tokens housekeeping started...")
        jwtRefreshTokenGrantRepository.deleteAllExpired()
        jwtRefreshTokenGrantRepository.flush()
        logger.info("Expired refresh tokens housekeeping complete!")
    }
}
