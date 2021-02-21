package io.mailsmr.application

import com.mercateo.test.clock.TestClock
import io.mailsmr.domain.JwtTokenFactory.Companion.JWT_ACCESS_TOKEN_VALIDITY_DURATION
import io.mailsmr.domain.JwtTokenFactory.Companion.JWT_REFRESH_TOKEN_VALIDITY_DURATION
import io.mailsmr.domain.errors.ExpiredOrRevokedTokenException
import io.mailsmr.domain.errors.InvalidCredentialsException
import io.mailsmr.domain.errors.InvalidTokenException
import io.mailsmr.domain.errors.UnauthorizedTokenCombinationException
import io.mailsmr.application.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Transactional
@SpringBootTest
internal class AuthenticationServiceTest {
    @TestConfiguration
    class TestClockConfig {
        @Bean
        fun clock(): Clock {
            val systemDefaultZone = Clock.systemDefaultZone()
            return TestClock.fixed(systemDefaultZone.instant(), systemDefaultZone.zone)
        }
    }

    @Autowired
    private lateinit var authenticationService: AuthenticationService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var clock: Clock

    @Test
    fun authenticateAndCreateJwtTokenPair_shouldCreateJwtTokens_ifUserExistsAndCredentialsAreCorrect() {
        // arrange
        val username = "username"
        val password = "password"
        userService.createUser(username, password, "recoveryEmailAddress")

        // act
        val (accessToken, refreshToken) = authenticationService.authenticateAndCreateJwtTokenPair(
            username, password
        )

        // assert
        assertTrue(accessToken.isValid())
        assertTrue(refreshToken.isValid())
    }

    @Test
    fun authenticateAndCreateJwtTokenPair_shouldThrow_ifUserDoesNotExist() {
        // arrange & act & assert
        assertThrows(InvalidCredentialsException::class.java) {
            authenticationService.authenticateAndCreateJwtTokenPair("username", "password")
        }
    }

    @Test
    fun authenticateAndCreateJwtTokenPair_shouldThrow_ifCredentialsAreWrong() {
        val username = "username"
        val password = "password"
        userService.createUser(username, password, "recoveryEmailAddress")

        // arrange & act & assert
        assertThrows(InvalidCredentialsException::class.java) {
            authenticationService.authenticateAndCreateJwtTokenPair(username, password + "error")
        }
    }

    @Test
    fun useRefreshTokenAndGenerateNewJwtTokenPair_shouldGenerateNewJwtTokenPair_ifPreviousAccessTokenIsValidAndRefreshTokenIsValid() {
        // arrange
        val username = "username"
        val password = "password"
        userService.createUser(username, password, "recoveryEmailAddress")

        val (accessToken, refreshToken) = authenticationService.authenticateAndCreateJwtTokenPair(
            username, password
        )

        // act
        val (newAccessToken, newRefreshToken) = authenticationService.useRefreshTokenAndGenerateNewJwtTokenPair(
            accessToken.toString(),
            refreshToken.toString()
        )

        // assert
        assertTrue(newAccessToken.isValid())
        assertTrue(newRefreshToken.isValid())
    }

    @Test
    fun useRefreshTokenAndGenerateNewJwtTokenPair_shouldGenerateNewJwtTokenPair_ifPreviousAccessTokenIsViableAndRefreshTokenIsValid() {
        val testClock: TestClock = clock as TestClock

        // arrange
        val username = "username"
        val password = "password"
        userService.createUser(username, password, "recoveryEmailAddress")

        val (accessToken, refreshToken) = authenticationService.authenticateAndCreateJwtTokenPair(
            username, password
        )

        testClock.fastForward(JWT_ACCESS_TOKEN_VALIDITY_DURATION.multipliedBy(2)) // jump in time and invalidate token

        // act
        val (newAccessToken, newRefreshToken) = authenticationService.useRefreshTokenAndGenerateNewJwtTokenPair(
            accessToken.toString(),
            refreshToken.toString()
        )

        // assert
        assertTrue(newAccessToken.isValid())
        assertTrue(newRefreshToken.isValid())
    }

    @Test
    fun useRefreshTokenAndGenerateNewJwtTokenPair_shouldThrow_ifPreviousRefreshTokenIsExpired() {
        val testClock: TestClock = clock as TestClock

        // arrange
        val username = "username"
        val password = "password"
        userService.createUser(username, password, "recoveryEmailAddress")

        val (accessToken, refreshToken) = authenticationService.authenticateAndCreateJwtTokenPair(
            username, password
        )

        testClock.fastForward(JWT_REFRESH_TOKEN_VALIDITY_DURATION.multipliedBy(2)) // jump in time and invalidate token

        // act & assert
        assertThrows(ExpiredOrRevokedTokenException::class.java) {
            val (newAccessToken, newRefreshToken) = authenticationService.useRefreshTokenAndGenerateNewJwtTokenPair(
                accessToken.toString(),
                refreshToken.toString()
            )
        }
    }

    @Test
    fun useRefreshTokenAndGenerateNewJwtTokenPair_shouldThrow_ifPreviousRefreshTokenIsNotViable() {
        val testClock: TestClock = clock as TestClock

        // arrange
        val username = "username"
        val password = "password"
        userService.createUser(username, password, "recoveryEmailAddress")

        val (accessToken, refreshToken) = authenticationService.authenticateAndCreateJwtTokenPair(
            username, password
        )

        testClock.fastForward(JWT_REFRESH_TOKEN_VALIDITY_DURATION.multipliedBy(2)) // jump in time and invalidate token

        // act & assert
        assertThrows(InvalidTokenException::class.java) {
            authenticationService.useRefreshTokenAndGenerateNewJwtTokenPair(
                accessToken.toString(),
                "RANDOM STRING - NOT VALID REFRESH TOKEN"
            )
        }
    }

    @Test
    fun useRefreshTokenAndGenerateNewJwtTokenPair_shouldThrow_ifPreviousAccessTokenIsNotViable() {
        val testClock: TestClock = clock as TestClock

        // arrange
        val username = "username"
        val password = "password"
        userService.createUser(username, password, "recoveryEmailAddress")

        val (accessToken, refreshToken) = authenticationService.authenticateAndCreateJwtTokenPair(
            username, password
        )

        testClock.fastForward(JWT_REFRESH_TOKEN_VALIDITY_DURATION.multipliedBy(2)) // jump in time and invalidate token

        // act & assert
        assertThrows(InvalidTokenException::class.java) {
            authenticationService.useRefreshTokenAndGenerateNewJwtTokenPair(
                "RANDOM STRING - NOT VALID ACCESS TOKEN",
                refreshToken.toString()
            )
        }
    }

    @Test
    fun useRefreshTokenAndGenerateNewJwtTokenPair_shouldThrow_ifPreviousAccessTokenIsAlreadyUsed() {
        // arrange
        val username = "username"
        val password = "password"
        userService.createUser(username, password, "recoveryEmailAddress")

        val (accessToken1, refreshToken1) = authenticationService.authenticateAndCreateJwtTokenPair(
            username, password
        )

        val (accessToken2, refreshToken2) = authenticationService.useRefreshTokenAndGenerateNewJwtTokenPair(
            accessToken1.toString(),
            refreshToken1.toString()
        )

        // act & assert
        assertThrows(ExpiredOrRevokedTokenException::class.java) {
            authenticationService.useRefreshTokenAndGenerateNewJwtTokenPair(
                accessToken2.toString(),
                refreshToken1.toString()
            )
        }
    }

    @Test
    fun useRefreshTokenAndGenerateNewJwtTokenPair_shouldThrow_ifUserOfAccessAndRefreshTokenDoNotMatch() {
        // arrange
        val username1 = "username"
        val password = "password"
        userService.createUser(username1, password, "recoveryEmailAddress")


        val username2 = "fake"
        userService.createUser(username2, password, "recoveryEmailAddress")

        val (accessToken1, refreshToken1) = authenticationService.authenticateAndCreateJwtTokenPair(
            username1, password
        )

        val (accessToken2, refreshToken2) = authenticationService.authenticateAndCreateJwtTokenPair(
            username2, password
        )

        // act & assert
        assertThrows(UnauthorizedTokenCombinationException::class.java) {
            authenticationService.useRefreshTokenAndGenerateNewJwtTokenPair(
                accessToken2.toString(),
                refreshToken1.toString()
            )
        }
    }

    @Test
    fun useRefreshTokenAndGenerateNewJwtTokenPair_shouldRevokeOldRefeshToken() {
        // arrange
        val username = "username"
        val password = "password"
        userService.createUser(username, password, "recoveryEmailAddress")

        val (accessToken1, refreshToken1) = authenticationService.authenticateAndCreateJwtTokenPair(
            username, password
        )

        val grants1 = authenticationService.getGrantedRefreshTokensForUsername(username)

        // act
        val (accessToken2, refreshToken2) = authenticationService.useRefreshTokenAndGenerateNewJwtTokenPair(
            accessToken1.toString(),
            refreshToken1.toString()
        )

        val grants2 = authenticationService.getGrantedRefreshTokensForUsername(username)

        // assert
        assertEquals(1, grants1.size)
        assertEquals(1, grants2.size)
        assertNotEquals(grants1[0].id, grants2[0].id)
        assertNotEquals(grants1[0].jti, grants2[0].jti)
    }


    @Test
    fun getGrantedRefreshTokensForUsername_shouldReturn1Grant_ifHas1Granted() {
        // arrange
        val username = "username"
        val password = "password"
        userService.createUser(username, password, "recoveryEmailAddress")

        val (accessToken, refreshToken) = authenticationService.authenticateAndCreateJwtTokenPair(
            username, password
        )

        // act
        val grants = authenticationService.getGrantedRefreshTokensForUsername(username)

        // assert
        assertEquals(1, grants.size)
    }


    @Test
    fun getGrantedRefreshTokensForUsername_shouldReturn2Grants_ifHas2Granted() {
        // arrange
        val username1 = "username1"
        val password1 = "password2"
        userService.createUser(username1, password1, "recoveryEmailAddress")

        authenticationService.authenticateAndCreateJwtTokenPair(username1, password1)

        authenticationService.authenticateAndCreateJwtTokenPair(username1, password1)

        // act
        val grants = authenticationService.getGrantedRefreshTokensForUsername(username1)

        // assert
        assertEquals(2, grants.size)
    }

    @Test
    fun getGrantedRefreshTokensForUsername_shouldReturn0Grants_ifHas1Expired() {
        val testClock: TestClock = clock as TestClock

        // arrange
        val username = "username"
        val password = "password"
        userService.createUser(username, password, "recoveryEmailAddress")

        authenticationService.authenticateAndCreateJwtTokenPair(username, password)

        testClock.fastForward(JWT_REFRESH_TOKEN_VALIDITY_DURATION.multipliedBy(2)) // jump in time and invalidate token

        // act
        val grants = authenticationService.getGrantedRefreshTokensForUsername(username)

        // assert
        assertEquals(0, grants.size)
    }

    @Test
    fun getGrantedRefreshTokensForUsername_shouldThrow_ifUserDoesNotExist() {
        // arrange
        val username = "username"
        val password = "password"
        userService.createUser(username, password, "recoveryEmailAddress")

        authenticationService.authenticateAndCreateJwtTokenPair(username, password)

        // act & assert
        assertThrows(InvalidCredentialsException::class.java) {
            authenticationService.getGrantedRefreshTokensForUsername(username + "error")
        }
    }
}
