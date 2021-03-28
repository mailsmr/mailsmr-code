package io.mailsmr.domain

import com.mercateo.test.clock.TestClock
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
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
internal class JwtTokenTest {
    @TestConfiguration
    class TestClockConfig {
        @Bean
        fun clock(): Clock {
            val systemDefaultZone = Clock.systemDefaultZone()
            return TestClock.fixed(systemDefaultZone.instant(), systemDefaultZone.zone)
        }
    }

    private class JwtTokenExtensionMock(jwtTokenString: String, jwtTokenSecret: String, clock: Clock) :
        JwtToken(jwtTokenString, jwtTokenSecret, clock)


    @Autowired
    private lateinit var clock: Clock

    @BeforeEach
    fun resetClock() {
        val testClock: TestClock = clock as TestClock
        val systemDefaultZone = Clock.systemDefaultZone()

        testClock.set(systemDefaultZone.instant())
    }

    @Test
    fun getIssueDate_shouldReturn_theDateOfCreation() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val username = "TestUserName"
        val jwtString = Jwts.builder().setClaims(HashMap()).setSubject(username).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtTokenExtensionMock(jwtString, jwtSecret, clock)

        // act
        val issuedAt = token.getIssuedAtDate()

        // assert
        assertEquals(issueDate.toString(), issuedAt.toString())
    }

    @Test
    fun getExpirationDate_shouldReturnCorrectExpirationDate() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val username = "TestUserName"
        val jwtString = Jwts.builder().setClaims(HashMap()).setSubject(username).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtTokenExtensionMock(jwtString, jwtSecret, clock)

        // act
        val returnedExpirationDate = token.getExpirationDate()

        // assert
        assertEquals(expirationDate.toString(), returnedExpirationDate.toString())
    }

    @Test
    fun isExpired_shouldReturnFalse_ifNotExpired() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val username = "TestUserName"
        val jwtString = Jwts.builder().setClaims(HashMap()).setSubject(username).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtTokenExtensionMock(jwtString, jwtSecret, clock)

        // act
        val isExpired = token.isExpired()

        // assert
        assertFalse(isExpired)
    }

    @Test
    fun isExpired_shouldReturnTrue_ifExpired() {
        val testClock: TestClock = clock as TestClock

        // arrange
        val jwtSecret = "secret"
        val expirationDuration = Duration.ofMinutes(30)
        val expirationDate = Date(clock.millis() + expirationDuration.toMillis())
        val issueDate = Date(clock.millis())
        val username = "TestUserName"
        val jwtString = Jwts.builder().setClaims(HashMap()).setSubject(username).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtTokenExtensionMock(jwtString, jwtSecret, clock)

        testClock.fastForward(expirationDuration.multipliedBy(2)) // jump in time and invalidate token

        // act
        val isExpired = token.isExpired()

        // assert
        assertTrue(isExpired)
    }

    @Test
    fun getUsername_shouldReturnUsername() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val username = "TestUserName"
        val jwtString = Jwts.builder().setClaims(HashMap()).setSubject(username).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtTokenExtensionMock(jwtString, jwtSecret, clock)

        // act
        val returnedUsername = token.getUsername()

        // assert
        assertEquals(username, returnedUsername)
    }

    @Test
    fun getUsername_shouldReturnNull_ifUsernameIsNotSet() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtTokenExtensionMock(jwtString, jwtSecret, clock)

        // act
        val returnedUsername = token.getUsername()

        // assert
        assertNull(returnedUsername)
    }

    @Test
    fun getUsername_shouldReturnUsername_evenIfTokenIsExpired() {
        val testClock: TestClock = clock as TestClock

        // arrange
        val jwtSecret = "secret"
        val username = "TestUserName"
        val expirationDuration = Duration.ofMinutes(30)
        val expirationDate = Date(clock.millis() + expirationDuration.toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setSubject(username).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtTokenExtensionMock(jwtString, jwtSecret, clock)

        testClock.fastForward(expirationDuration.multipliedBy(2)) // jump in time and invalidate token

        // act
        val returnedUsername = token.getUsername()

        // assert
        assertEquals(username, returnedUsername)
    }

    @Test
    fun isUsernameSet_shouldReturnTrue_ifSet() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val username = "TestUserName"
        val jwtString = Jwts.builder().setClaims(HashMap()).setSubject(username).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtTokenExtensionMock(jwtString, jwtSecret, clock)

        // act
        val isUsernameSet = token.isUsernameSet()

        // assert
        assertTrue(isUsernameSet)
    }

    @Test
    fun isUsernameSet_shouldReturnFalse_ifNotSet() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtTokenExtensionMock(jwtString, jwtSecret, clock)

        // act
        val isUsernameSet = token.isUsernameSet()

        // assert
        assertFalse(isUsernameSet)
    }

    @Test
    fun testToString() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtTokenExtensionMock(jwtString, jwtSecret, clock)

        // act
        val tokenToString = token.toString()

        // assert
        assertEquals(jwtString, tokenToString)
    }

    @Test
    fun isViable_shouldBeTrue_ifHasCorrectSecretAndIsNotExpired() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtTokenExtensionMock(jwtString, jwtSecret, clock)

        // act
        val isViable = token.isViable()

        // assert
        assertTrue(isViable)
    }

    @Test
    fun isViable_shouldBeTrue_ifHasCorrectSecretAndIsExpired() {
        val testClock: TestClock = clock as TestClock

        // arrange
        val jwtSecret = "secret"
        val username = "TestUserName"
        val expirationDuration = Duration.ofMinutes(30)
        val expirationDate = Date(clock.millis() + expirationDuration.toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setSubject(username).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtTokenExtensionMock(jwtString, jwtSecret, clock)

        testClock.fastForward(expirationDuration.multipliedBy(2)) // jump in time and invalidate token

        // act
        val isViable = token.isViable()

        // assert
        assertTrue(token.isExpired())
        assertTrue(isViable)
    }

    @Test
    fun isViable_shouldBeFalse_ifHasWrongSecret() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtTokenExtensionMock(jwtString, jwtSecret + "someRandomCharacters", clock)

        // act
        val isViable = token.isViable()

        // assert
        assertFalse(isViable)
    }

    @Test
    fun isViable_shouldBeFalse_ifIsAmbiguousJwtString() {
        // arrange
        val token = JwtTokenExtensionMock("jwtString", "secret", clock)

        // act
        val isViable = token.isViable()

        // assert
        assertFalse(isViable)
    }

    @Test
    fun isValid_shouldBeTrue_ifHasCorrectSecretAndIsNotExpired() {
        // arrange
        val jwtSecret = "secret"
        val expirationDate = Date(clock.millis() + Duration.ofMinutes(30).toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtTokenExtensionMock(jwtString, jwtSecret, clock)

        // act
        val isValid = token.isValid()

        // assert
        assertTrue(isValid)
    }

    @Test
    fun isValid_shouldBeFalse_ifHasCorrectSecretAndIsExpired() {
        val testClock: TestClock = clock as TestClock

        // arrange
        val jwtSecret = "secret"
        val username = "TestUserName"
        val expirationDuration = Duration.ofMinutes(30)
        val expirationDate = Date(clock.millis() + expirationDuration.toMillis())
        val issueDate = Date(clock.millis())
        val jwtString = Jwts.builder().setClaims(HashMap()).setSubject(username).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtTokenExtensionMock(jwtString, jwtSecret, clock)

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
        val jwtString = Jwts.builder().setClaims(HashMap()).setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()

        val token = JwtTokenExtensionMock(jwtString, jwtSecret + "someRandomCharacters", clock)

        // act
        val isValid = token.isValid()

        // assert
        assertFalse(isValid)
    }

    @Test
    fun isValid_shouldBeFalse_ifIsAmbiguousJwtString() {
        // arrange
        val token = JwtTokenExtensionMock("jwtString", "secret", clock)

        // act
        val isValid = token.isValid()

        // assert
        assertFalse(isValid)
    }

}
