package io.mailsmr.domain

import com.mercateo.test.clock.TestClock
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.time.Clock
import java.time.Duration
import java.util.*
import kotlin.collections.HashMap

@SpringBootTest
internal class JwtRefreshTokenTest {
    @TestConfiguration
    class TestClockConfig {
        @Bean
        fun clock(): Clock {
            val systemDefaultZone = Clock.systemDefaultZone()
            return TestClock.fixed(systemDefaultZone.instant(), systemDefaultZone.zone)
        }
    }

    @Autowired
    private lateinit var clock: Clock


    @Test
    fun isViable_shouldBeTrue_ifHasCorrectSecretAJtiAndIsNotExpired() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate).setId(UUID.randomUUID().toString())
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtRefreshToken(jwtString, jwtSecret, clock)

        // act
        val isViable = token.isViable()

        // assert
        assertTrue(isViable)
    }

    @Test
    fun isViable_shouldBeTrue_ifHasCorrectSecretAJtiAndIsExpired() {
        val testClock: TestClock = clock as TestClock

        // arrange
        val jwtSecret = "secret"
        val username = "TestUserName"
        val expirationDuration = Duration.ofMinutes(30)
        val expirationDate = Date(clock.millis() + expirationDuration.toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate).setId(UUID.randomUUID().toString())
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtRefreshToken(jwtString, jwtSecret, clock)

        testClock.fastForward(expirationDuration.multipliedBy(2)) // jump in time and invalidate token

        // act
        val isViable = token.isViable()

        // assert
        assertTrue(isViable)
    }

    @Test
    fun isViable_shouldBeFalse_ifHasNoJtiSet() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtRefreshToken(jwtString, jwtSecret, clock)

        // act
        val isViable = token.isViable()

        // assert
        assertFalse(isViable)
    }


    @Test
    fun isViable_shouldBeFalse_ifHasWrongSecret() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate).setId(UUID.randomUUID().toString())
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtRefreshToken(jwtString, jwtSecret + "someRandomCharacters", clock)

        // act
        val isViable = token.isViable()

        // assert
        assertFalse(isViable)
    }

    @Test
    fun isViable_shouldBeFalse_ifIsAmbiguousJwtString() {
        // arrange
        val token = JwtRefreshToken("jwtString", "secret", clock)

        // act
        val isViable = token.isViable()

        // assert
        assertFalse(isViable)
    }

    @Test
    fun isValid_shouldBeTrue_ifHasCorrectSecretAJtiAndIsNotExpired() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate).setId(UUID.randomUUID().toString())
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtRefreshToken(jwtString, jwtSecret, clock)

        // act
        val isValid = token.isValid()

        // assert
        assertTrue(isValid)
    }

    @Test
    fun isValid_shouldBeFalse_ifHasCorrectSecretAJtiAndIsExpired() {
        val testClock: TestClock = clock as TestClock

        // arrange
        val jwtSecret = "secret"
        val username = "TestUserName"
        val expirationDuration = Duration.ofMinutes(30)
        val expirationDate = Date(clock.millis() + expirationDuration.toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate).setId(UUID.randomUUID().toString())
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtRefreshToken(jwtString, jwtSecret, clock)

        testClock.fastForward(expirationDuration.multipliedBy(2)) // jump in time and invalidate token

        // act
        val isValid = token.isValid()

        // assert
        assertFalse(isValid)
    }

    @Test
    fun isValid_shouldBeFalse_ifHasWrongSecret() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate).setId(UUID.randomUUID().toString())
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtRefreshToken(jwtString, jwtSecret + "someRandomCharacters", clock)

        // act
        val isValid = token.isValid()

        // assert
        assertFalse(isValid)
    }

    @Test
    fun isValid_shouldBeFalse_ifHasNoJtiSet() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtRefreshToken(jwtString, jwtSecret, clock)

        // act
        val isValid = token.isValid()

        // assert
        assertFalse(isValid)
    }

    @Test
    fun isValid_shouldBeFalse_ifIsAmbiguousJwtString() {
        // arrange
        val token = JwtRefreshToken("jwtString", "secret", clock)

        // act
        val isValid = token.isValid()

        // assert
        assertFalse(isValid)
    }

    @Test
    fun getJti_shouldReturnJti_ifSet() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val jti = UUID.randomUUID().toString()
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate).setId(jti)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtRefreshToken(jwtString, jwtSecret, clock)

        // act
        val returnedJti = token.getJti()

        // assert
        assertEquals(jti, returnedJti)
    }

    @Test
    fun getJti_shouldReturnNull_ifNotSet() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtRefreshToken(jwtString, jwtSecret, clock)

        // act
        val returnedJti = token.getJti()

        // assert
        assertNull(returnedJti)
    }
}
