package io.mailsmr.application

import com.mercateo.test.clock.TestClock
import io.mailsmr.common.infrastructure.repositories.JwtRefreshTokenGrantRepository
import io.mailsmr.domain.JwtTokenFactoryService
import io.mailsmr.domain.JwtTokenFactoryService.Companion.JWT_REFRESH_TOKEN_VALIDITY_DURATION
import io.mailsmr.domain.errors.ExpiredOrRevokedTokenException
import io.mailsmr.domain.errors.InvalidTokenException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@SpringBootTest
@Transactional
internal class JwtRefreshTokenGrantServiceTest {

    @TestConfiguration
    class TestClockConfig {
        @Bean
        fun clock(): Clock {
            val systemDefaultZone = Clock.systemDefaultZone()
            return TestClock.fixed(systemDefaultZone.instant(), systemDefaultZone.zone)
        }
    }

    @Autowired
    private lateinit var jwtRefreshTokenGrantService: JwtRefreshTokenGrantService

    @Autowired
    private lateinit var jwtTokenFactoryService: JwtTokenFactoryService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var jwtRefreshTokenGrantRepository: JwtRefreshTokenGrantRepository

    @Autowired
    private lateinit var clock: Clock

    @BeforeEach
    fun resetClock() {
        val testClock: TestClock = clock as TestClock
        val systemDefaultZone = Clock.systemDefaultZone()

        testClock.set(systemDefaultZone.instant())
    }

    @Test
    fun isRefreshTokenGranted_throws_ifInvalidToken() {
        // arrange
        val refreshToken = jwtTokenFactoryService.fromRefreshTokenString("INVALID")

        // act & assert
        assertThrows(InvalidTokenException::class.java) { jwtRefreshTokenGrantService.isRefreshTokenGranted(refreshToken) }
    }

    @Test
    fun isRefreshTokenGranted_returnsTrue_ifValidAndNotExpiredToken() {
        // arrange
        val username = "TestUser"
        val password = "password"
        val user = userService.createUser(username, password, "test@test.com")
        val refreshToken = jwtTokenFactoryService.generateRefreshToken(username)

        jwtRefreshTokenGrantService.grantRefreshToken(refreshToken, user)


        // act
        val isRefreshTokenGranted = jwtRefreshTokenGrantService.isRefreshTokenGranted(refreshToken)

        // assert
        assertTrue(isRefreshTokenGranted)
    }

    @Test
    fun isRefreshTokenGranted_returnsFalse_ifTokenDoesNotExist() {
        // arrange
        val username = "TestUser"
        val refreshToken = jwtTokenFactoryService.generateRefreshToken(username)

        // act
        val isRefreshTokenGranted = jwtRefreshTokenGrantService.isRefreshTokenGranted(refreshToken)

        // assert
        assertFalse(isRefreshTokenGranted)
    }

    @Test
    fun isRefreshTokenGranted_returnsFalse_ifTokenIsExpired() {
        val testClock: TestClock = clock as TestClock

        // arrange
        val username = "TestUser"
        val password = "password"
        val user = userService.createUser(username, password, "test@test.com")
        val refreshToken = jwtTokenFactoryService.generateRefreshToken(username)

        jwtRefreshTokenGrantService.grantRefreshToken(refreshToken, user)

        testClock.fastForward(JWT_REFRESH_TOKEN_VALIDITY_DURATION.multipliedBy(2))

        // act
        val isRefreshTokenGranted = jwtRefreshTokenGrantService.isRefreshTokenGranted(refreshToken)

        // assert
        assertFalse(isRefreshTokenGranted)
    }

    @Test
    fun isRefreshTokenGranted_shouldDeleteExpiredTokenFromDatabase() {
        val testClock: TestClock = clock as TestClock

        // arrange
        val username = "TestUser"
        val password = "password"
        val user = userService.createUser(username, password, "test@test.com")
        val refreshToken = jwtTokenFactoryService.generateRefreshToken(username)

        jwtRefreshTokenGrantService.grantRefreshToken(refreshToken, user)

        testClock.fastForward(JWT_REFRESH_TOKEN_VALIDITY_DURATION.multipliedBy(2))
        assertTrue(jwtRefreshTokenGrantRepository.existsByJti(refreshToken.getJti()!!))

        // act
        jwtRefreshTokenGrantService.isRefreshTokenGranted(refreshToken)

        // assert
        assertFalse(jwtRefreshTokenGrantRepository.existsByJti(refreshToken.getJti()!!))
    }

    @Test
    fun revokeRefreshToken_throws_ifInvalidToken() {
        // arrange
        val refreshToken = jwtTokenFactoryService.fromRefreshTokenString("INVALID")

        // act & assert
        assertThrows(InvalidTokenException::class.java) { jwtRefreshTokenGrantService.revokeRefreshToken(refreshToken) }
    }

    @Test
    fun revokeRefreshToken_shouldDeleteTokenFromDatabase_ifRefreshTokenDoesExist() {
        // arrange
        val username = "TestUser"
        val password = "password"
        val user = userService.createUser(username, password, "test@test.com")
        val refreshToken = jwtTokenFactoryService.generateRefreshToken(username)

        jwtRefreshTokenGrantService.grantRefreshToken(refreshToken, user)

        assertTrue(jwtRefreshTokenGrantRepository.existsByJti(refreshToken.getJti()!!))

        // act
        jwtRefreshTokenGrantService.revokeRefreshToken(refreshToken)

        // assert
        assertFalse(jwtRefreshTokenGrantRepository.existsByJti(refreshToken.getJti()!!))
    }

    @Test
    fun revokeRefreshToken_shouldNotThrow_ifRefreshTokenDoesNotExist() {
        // arrange
        val username = "TestUser"
        val refreshToken = jwtTokenFactoryService.generateRefreshToken(username)

        // act & assert
        assertDoesNotThrow { jwtRefreshTokenGrantService.revokeRefreshToken(refreshToken) }
    }

    @Test
    fun grantRefreshToken_throws_ifInvalidToken() {
        // arrange
        val username = "TestUser"
        val password = "password"
        val user = userService.createUser(username, password, "test@test.com")
        val refreshToken = jwtTokenFactoryService.fromRefreshTokenString("INVALID")

        // act & assert
        assertThrows(InvalidTokenException::class.java) {
            jwtRefreshTokenGrantService.grantRefreshToken(
                refreshToken,
                user
            )
        }
    }

    @Test
    fun grantRefreshToken_throws_ifExpiredToken() {
        val testClock: TestClock = clock as TestClock

        // arrange
        val username = "TestUser"
        val password = "password"
        val user = userService.createUser(username, password, "test@test.com")
        val refreshToken = jwtTokenFactoryService.generateRefreshToken(username)

        testClock.fastForward(JWT_REFRESH_TOKEN_VALIDITY_DURATION.multipliedBy(2))

        // act & assert
        assertThrows(ExpiredOrRevokedTokenException::class.java) {
            jwtRefreshTokenGrantService.grantRefreshToken(
                refreshToken,
                user
            )
        }
    }

    @Test
    fun grantRefreshToken_shouldCreateTokenGrantAndStoreItToTheDB() {
        // arrange
        val username = "TestUser"
        val password = "password"
        val user = userService.createUser(username, password, "test@test.com")
        val refreshToken = jwtTokenFactoryService.generateRefreshToken(username)

        // act
        jwtRefreshTokenGrantService.grantRefreshToken(refreshToken, user)

        // assert
        val grant = jwtRefreshTokenGrantRepository.findByJti(refreshToken.getJti()!!)!!

        assertEquals(LocalDateTime.now(clock), grant.creationDateTime)
        assertEquals(
            LocalDateTime.now(clock).plus(JWT_REFRESH_TOKEN_VALIDITY_DURATION).truncatedTo(ChronoUnit.SECONDS),
            grant.expirationDateTime
        )
        assertEquals(user, grant.user)
    }

    @Test
    fun grantRefreshToken_shouldCreateTokenGrantAndStoreItToTheDB_IfDuplicate() {
        // arrange
        val username = "TestUser"
        val password = "password"
        val user = userService.createUser(username, password, "test@test.com")
        val refreshToken = jwtTokenFactoryService.generateRefreshToken(username)

        // act
        jwtRefreshTokenGrantService.grantRefreshToken(refreshToken, user)

        // assert
        assertDoesNotThrow { jwtRefreshTokenGrantService.grantRefreshToken(refreshToken, user) }
    }

    @Test
    fun getGrantedRefreshTokensForUser_0() {
        // arrange
        val username = "TestUser"
        val password = "password"
        val user = userService.createUser(username, password, "test@test.com")

        // act
        val grants = jwtRefreshTokenGrantService.getGrantedRefreshTokensForUser(user)

        // assert
        assertEquals(0, grants.size)
    }

    @Test
    fun getGrantedRefreshTokensForUser_1() {
        // arrange
        val username = "TestUser"
        val password = "password"
        val user = userService.createUser(username, password, "test@test.com")

        val refreshToken1 = jwtTokenFactoryService.generateRefreshToken(username)
        jwtRefreshTokenGrantService.grantRefreshToken(refreshToken1, user)

        // act
        val grants = jwtRefreshTokenGrantService.getGrantedRefreshTokensForUser(user)

        // assert
        assertEquals(1, grants.size)
    }

    @Test
    fun getGrantedRefreshTokensForUser_2() {
        // arrange
        val username = "TestUser"
        val password = "password"
        val user = userService.createUser(username, password, "test@test.com")

        val refreshToken1 = jwtTokenFactoryService.generateRefreshToken(username)
        jwtRefreshTokenGrantService.grantRefreshToken(refreshToken1, user)
        val refreshToken2 = jwtTokenFactoryService.generateRefreshToken(username)
        jwtRefreshTokenGrantService.grantRefreshToken(refreshToken2, user)

        // act
        val grants = jwtRefreshTokenGrantService.getGrantedRefreshTokensForUser(user)

        // assert
        assertEquals(2, grants.size)
    }


    @Test
    fun getGrantedRefreshTokensForUser_1_filterExpired() {
        val testClock: TestClock = clock as TestClock

        // arrange
        val username = "TestUser"
        val password = "password"
        val user = userService.createUser(username, password, "test@test.com")

        val refreshToken1 = jwtTokenFactoryService.generateRefreshToken(username)
        jwtRefreshTokenGrantService.grantRefreshToken(refreshToken1, user)
        testClock.fastForward(JWT_REFRESH_TOKEN_VALIDITY_DURATION.multipliedBy(2))

        val refreshToken2 = jwtTokenFactoryService.generateRefreshToken(username)
        jwtRefreshTokenGrantService.grantRefreshToken(refreshToken2, user)

        // act
        val grants = jwtRefreshTokenGrantService.getGrantedRefreshTokensForUser(user)

        // assert
        assertEquals(1, grants.size)
    }

    @Test
    fun deleteExpiredRefreshTokensFromDatabase() {
        val testClock: TestClock = clock as TestClock

        // arrange
        val username = "TestUser"
        val password = "password"
        val user = userService.createUser(username, password, "test@test.com")

        val refreshToken1 = jwtTokenFactoryService.generateRefreshToken(username)
        jwtRefreshTokenGrantService.grantRefreshToken(refreshToken1, user)

        // rewind so that the token gets generated in the past as the cleanup task happens on the db with the DB time
        testClock.rewind(JWT_REFRESH_TOKEN_VALIDITY_DURATION.multipliedBy(2))

        val refreshToken2 = jwtTokenFactoryService.generateRefreshToken(username)
        jwtRefreshTokenGrantService.grantRefreshToken(refreshToken2, user)

        assertEquals(2, jwtRefreshTokenGrantRepository.findAllByUser(user).count())

        // act
        jwtRefreshTokenGrantService.deleteExpiredRefreshTokensFromDatabase()

        // assert
        assertEquals(1, jwtRefreshTokenGrantRepository.findAllByUser(user).count())
    }
}
