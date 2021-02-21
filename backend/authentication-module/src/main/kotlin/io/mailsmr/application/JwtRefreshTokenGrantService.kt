package io.mailsmr.application

import io.mailsmr.domain.JwtRefreshToken
import io.mailsmr.domain.errors.ExpiredOrRevokedTokenException
import io.mailsmr.domain.errors.InvalidTokenException
import io.mailsmr.common.infrastructure.entities.JwtRefreshTokenGrant
import io.mailsmr.common.infrastructure.entities.User
import io.mailsmr.common.infrastructure.repositories.JwtRefreshTokenGrantRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime
import java.util.stream.Collectors

@Service
class JwtRefreshTokenGrantService(
    private val jwtRefreshTokenGrantRepository: JwtRefreshTokenGrantRepository,
    private val clock: Clock
) {
//    TODO cron job for removing expired refresh tokens

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

            jwtRefreshTokenGrantRepository.saveAndFlush(refreshTokenGrant)
        } else {
            throw InvalidTokenException()
        }
    }

    fun getGrantedRefreshTokensForUser(user: User): List<JwtRefreshTokenGrant> {
        return jwtRefreshTokenGrantRepository.findAllByUser(user).parallelStream()
            .filter { token -> token.expirationDateTime.isAfter(LocalDateTime.now(clock)) }
            .collect(Collectors.toList())
    }
}
