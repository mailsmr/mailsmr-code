package io.mailsmr.application

import io.mailsmr.application.util.AESDecryptionUtil.encryptPassword
import io.mailsmr.common.infrastructure.entities.JwtRefreshTokenGrant
import io.mailsmr.common.infrastructure.entities.User
import io.mailsmr.common.infrastructure.repositories.UserRepository
import io.mailsmr.domain.JwtAccessToken
import io.mailsmr.domain.JwtRefreshToken
import io.mailsmr.domain.JwtTokenFactory
import io.mailsmr.domain.JwtTokenPair
import io.mailsmr.domain.errors.ExpiredOrRevokedTokenException
import io.mailsmr.domain.errors.InvalidCredentialsException
import io.mailsmr.domain.errors.InvalidTokenException
import io.mailsmr.domain.errors.UnauthorizedTokenCombinationException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val jwtTokenFactory: JwtTokenFactory,
    private val jwtRefreshTokenGrantService: JwtRefreshTokenGrantService,
    private val authenticationManager: AuthenticationManager,
) {

    fun authenticateAndCreateJwtTokenPair(username: String, password: String): JwtTokenPair {
        val invalidCredentialsExceptionMessage = "Username or password is not correct."
        if (!userService.userExists(username)) throw InvalidCredentialsException(invalidCredentialsExceptionMessage)

        try {
            authenticate(username, password)
        } catch (e: BadCredentialsException) {
            throw InvalidCredentialsException(invalidCredentialsExceptionMessage)
        }

        val user: User = userRepository.findByUsername(username)!!
        val encryptedMasterPassword = encryptPassword(password, user.masterKeyEncryptionSecret)

        return generateAndGrantJwtTokenPair(user, encryptedMasterPassword)
    }

    fun useRefreshTokenAndGenerateNewJwtTokenPair(
        previousAccessTokenString: String,
        currentRefreshTokenString: String
    ): JwtTokenPair {
        val currentRefreshToken: JwtRefreshToken = jwtTokenFactory.fromRefreshTokenString(currentRefreshTokenString)
        val previousAccessToken: JwtAccessToken = jwtTokenFactory.fromAccessTokenString(previousAccessTokenString)

        if (currentRefreshToken.isViable() && previousAccessToken.isViable() && currentRefreshToken.isUsernameSet()) {
            matchingTokensCheckBarrier(previousAccessToken, currentRefreshToken)
            refreshTokenExpirationAndGrantCheckBarrier(currentRefreshToken)

            // Get User Details
            val user: User = userRepository.findByUsername(currentRefreshToken.getUsername()!!)!!

            // Generate new tokens
            val encryptedMasterPassword: String = previousAccessToken.getEncryptedMasterPassword()!!

            val newJwtTokenPair = generateAndGrantJwtTokenPair(user, encryptedMasterPassword)
            jwtRefreshTokenGrantService.revokeRefreshToken(currentRefreshToken)

            return newJwtTokenPair
        }

        throw InvalidTokenException()
    }

    private fun authenticate(username: String, password: String) {
        try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, password))
        } catch (e: DisabledException) {
            throw DisabledException("USER_DISABLED", e)
        } catch (e: BadCredentialsException) {
            throw BadCredentialsException("INVALID_CREDENTIALS", e)
        }
    }

    private fun generateAndGrantJwtTokenPair(
        user: User,
        encryptedMasterPassword: String
    ): JwtTokenPair {
        val accessToken: JwtAccessToken = jwtTokenFactory.generateAccessToken(
            user.username, encryptedMasterPassword
        )
        val refreshToken: JwtRefreshToken = jwtTokenFactory.generateRefreshToken(user.username)

        jwtRefreshTokenGrantService.grantRefreshToken(refreshToken, user)

        return Pair(accessToken, refreshToken)
    }


    private fun matchingTokensCheckBarrier(accessToken: JwtAccessToken, refreshToken: JwtRefreshToken) {
        if (!refreshToken.getUsername().equals(accessToken.getUsername()))
            throw UnauthorizedTokenCombinationException()
    }

    private fun refreshTokenExpirationAndGrantCheckBarrier(refreshToken: JwtRefreshToken) {
        if (refreshToken.isExpired() ||
            !jwtRefreshTokenGrantService.isRefreshTokenGranted(refreshToken)
        ) throw ExpiredOrRevokedTokenException("The provided refresh token is expired, used or revoked.")
    }

    fun getGrantedRefreshTokensForUsername(name: String): List<JwtRefreshTokenGrant> {
        val user = userRepository.findByUsername(name) ?: throw InvalidCredentialsException()

        return jwtRefreshTokenGrantService.getGrantedRefreshTokensForUser(user)
    }
}
