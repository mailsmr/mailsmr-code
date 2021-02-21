package io.mailsmr.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import java.security.InvalidKeyException
import java.time.Clock
import java.time.Duration
import java.util.*

@SpringBootTest
internal class JwtTokenFactoryTest {

    @Autowired
    private lateinit var jwtTokenFactory: JwtTokenFactory

    @Autowired
    private lateinit var clock: Clock

    @Test
    fun fromRequest_shouldReturnToken_ifPresent() {
        // arrange
        val jwtTokenString = "TEST_JWT_TOKEN"
        val mockHttpServletRequest = MockHttpServletRequest()
        mockHttpServletRequest.addHeader("Authorization", "Bearer $jwtTokenString")

        // act
        val token = jwtTokenFactory.fromRequest(mockHttpServletRequest)

        // assert
        assertEquals(jwtTokenString, token.toString())
    }

    @Test
    fun fromRequest_shouldReturnNull_ifAuthorizationHeaderIsNotPresent() {
        // arrange
        val mockHttpServletRequest = MockHttpServletRequest()

        // act
        val token = jwtTokenFactory.fromRequest(mockHttpServletRequest)

        // assert
        assertNull(token)
    }

    @Test
    fun fromAuthorizationHeader_shouldReturnToken_ifValid() {
        // arrange
        val jwtTokenString = "TEST_JWT_TOKEN"
        val authorizationHeader = "Bearer $jwtTokenString"

        // act
        val token = jwtTokenFactory.fromAuthorizationHeader(authorizationHeader)

        // assert
        assertEquals(jwtTokenString, token.toString())
    }

    @Test
    fun fromAuthorizationHeader_shouldThrow_ifInvalid() {
        // arrange
        val jwtTokenString = "TEST_JWT_TOKEN"
        val authorizationHeader = "Baerer $jwtTokenString" // is Baerer instead of Bearer

        // act & assert
        assertThrows(InvalidKeyException::class.java) { jwtTokenFactory.fromAuthorizationHeader(authorizationHeader) }
    }

    @Test
    fun fromRefreshTokenString_shouldConvertStringToRefreshToken() {
        // arrange
        val jwtTokenString = "TEST_JWT_TOKEN"

        // act
        val token = jwtTokenFactory.fromRefreshTokenString(jwtTokenString)

        // assert
        assertTrue(token is JwtRefreshToken)
    }

    @Test
    fun fromAccessTokenString() {
        // arrange
        val jwtTokenString = "TEST_JWT_TOKEN"

        // act
        val token = jwtTokenFactory.fromAccessTokenString(jwtTokenString)

        // assert
        assertTrue(token is JwtAccessToken)
    }

    @Test
    fun generateAccessToken_shouldCreateValidAccessToken_withDefaultValidity() {
        // arrange
        val username = "username"
        val encryptedMasterPassword = "encryptedMasterPassword"

        // act
        val token = jwtTokenFactory.generateAccessToken(username, encryptedMasterPassword)

        // assert
        assertTrue(token.isValid())
        assertEquals(username, token.getUsername())
        assertEquals(encryptedMasterPassword, token.getEncryptedMasterPassword())
        assertEquals(
            Date(clock.millis()).toString(),
            token.getIssuedAtDate().toString()
        )
        assertEquals(
            Date(clock.millis() + JwtTokenFactory.JWT_ACCESS_TOKEN_VALIDITY_DURATION.toMillis()).toString(),
            token.getExpirationDate().toString()
        )
    }

    @Test
    fun generateAccessToken_shouldCreateValidAccessToken_withCustomValidity() {
        // arrange
        val username = "username"
        val encryptedMasterPassword = "encryptedMasterPassword"
        val expirationDuration = Duration.ofMinutes(5)

        // act
        val token = jwtTokenFactory.generateAccessToken(username, encryptedMasterPassword, expirationDuration)

        // assert
        assertTrue(token.isValid())
        assertEquals(username, token.getUsername())
        assertEquals(encryptedMasterPassword, token.getEncryptedMasterPassword())
        assertEquals(
            Date(clock.millis() + expirationDuration.toMillis()).toString(),
            token.getExpirationDate().toString()
        )
    }


    @Test
    fun generateRefreshToken_shouldCreateValidAccessToken_withDefaultValidity() {
        // arrange
        val username = "username"
        val encryptedMasterPassword = "encryptedMasterPassword"

        // act
        val token = jwtTokenFactory.generateRefreshToken(username)

        // assert
        assertTrue(token.isValid())
        assertEquals(username, token.getUsername())
        assertNotNull(token.getJti())
        assertEquals(
            Date(clock.millis()).toString(),
            token.getIssuedAtDate().toString()
        )
        assertEquals(
            Date(clock.millis() + JwtTokenFactory.JWT_REFRESH_TOKEN_VALIDITY_DURATION.toMillis()).toString(),
            token.getExpirationDate().toString()
        )
    }

    @Test
    fun generateRefreshToken_shouldCreateValidAccessToken_withCustomValidity() {
        // arrange
        val username = "username"
        val encryptedMasterPassword = "encryptedMasterPassword"
        val expirationDuration = Duration.ofMinutes(5)

        // act
        val token = jwtTokenFactory.generateRefreshToken(username, expirationDuration)

        // assert
        assertTrue(token.isValid())
        assertEquals(username, token.getUsername())
        assertNotNull(token.getJti())
        assertEquals(
            Date(clock.millis()).toString(),
            token.getIssuedAtDate().toString()
        )
        assertEquals(
            Date(clock.millis() + expirationDuration.toMillis()).toString(),
            token.getExpirationDate().toString()
        )
    }
}
