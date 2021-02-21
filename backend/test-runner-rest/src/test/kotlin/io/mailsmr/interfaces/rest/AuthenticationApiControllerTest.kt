package io.mailsmr.interfaces.rest

import io.mailsmr.domain.JwtTokenFactory.Companion.JWT_ACCESS_TOKEN_VALIDITY_DURATION
import io.mailsmr.domain.JwtTokenFactory.Companion.JWT_REFRESH_TOKEN_VALIDITY_DURATION
import io.mailsmr.interfaces.rest.AuthenticationApiPaths.AUTHENTICATION_BASE_PATH
import io.mailsmr.interfaces.rest.AuthenticationApiPaths.GET__LIST_GRANTED_REFRESH_TOKENS
import io.mailsmr.interfaces.rest.AuthenticationApiPaths.POST__CREATE_AUTHENTICATION_TOKEN_PATH
import io.mailsmr.interfaces.rest.AuthenticationApiPaths.POST__USE_REFRESH_TOKEN_PATH
import io.mailsmr.interfaces.rest.dtos.AuthenticationResponseDto
import io.mailsmr.interfaces.rest.dtos.AuthenticationTokenRefreshRequestDto
import io.mailsmr.interfaces.rest.dtos.AuthenticationTokenRequestDto
import io.mailsmr.interfaces.rest.dtos.GrantedRefreshTokenDTO
import io.mailsmr.helpers.RestTestSessionHelper
import io.mailsmr.helpers.RestTestSessionHelper.Companion.PASSWORD
import io.mailsmr.helpers.RestTestSessionHelper.Companion.USERNAME
import io.mailsmr.interfaces.rest.UserApiPaths.POST__CREATE_USER_PATH
import io.mailsmr.interfaces.rest.UserApiPaths.USER_BASE_PATH
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mercateo.test.clock.TestClock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.Clock
import javax.transaction.Transactional


@Transactional
@AutoConfigureMockMvc
@SpringBootTest
internal class AuthenticationApiControllerTest {

    @TestConfiguration
    class TestClockConfig {
        @Bean
        fun clock(): Clock {
            val systemDefaultZone = Clock.systemDefaultZone()
            return TestClock.fixed(systemDefaultZone.instant(), systemDefaultZone.zone)
        }
    }


    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var restTestSessionHelper: RestTestSessionHelper

    @Autowired
    private lateinit var clock: Clock

    @Test
    fun createAuthenticationToken_shouldReturnNewToken_withValidUsernameAndPassword_200() {
        restTestSessionHelper.getValidAuthenticationResponseDtoForTestUser()

        val authenticationTokenRequestDto = AuthenticationTokenRequestDto(USERNAME, PASSWORD)

        this.mockMvc.perform(
            MockMvcRequestBuilders.post("$AUTHENTICATION_BASE_PATH$POST__CREATE_AUTHENTICATION_TOKEN_PATH")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(authenticationTokenRequestDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun createAuthenticationToken_shouldFail_withValidUsernameAndInvalidPassword_422() {
        restTestSessionHelper.getValidAuthenticationResponseDtoForTestUser()

        val authenticationTokenRequestDto = AuthenticationTokenRequestDto(USERNAME, "${PASSWORD}FAULT")

        this.mockMvc.perform(
            MockMvcRequestBuilders.post("$AUTHENTICATION_BASE_PATH$POST__CREATE_AUTHENTICATION_TOKEN_PATH")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(authenticationTokenRequestDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    fun createAuthenticationToken_shouldFail_withInvalidUsernameAndValidPassword_422() {
        restTestSessionHelper.getValidAuthenticationResponseDtoForTestUser()

        val authenticationTokenRequestDto = AuthenticationTokenRequestDto("${USERNAME}FAULT", PASSWORD)

        this.mockMvc.perform(
            MockMvcRequestBuilders.post("$AUTHENTICATION_BASE_PATH$POST__CREATE_AUTHENTICATION_TOKEN_PATH")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(authenticationTokenRequestDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    fun createAuthenticationToken_shouldFail_withInvalidUsernameAndInvalidPassword_422() {
        restTestSessionHelper.getValidAuthenticationResponseDtoForTestUser()

        val authenticationTokenRequestDto = AuthenticationTokenRequestDto("${USERNAME}FAULT", "${PASSWORD}FAULT")

        this.mockMvc.perform(
            MockMvcRequestBuilders.post("$AUTHENTICATION_BASE_PATH$POST__CREATE_AUTHENTICATION_TOKEN_PATH")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(authenticationTokenRequestDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }


    @Test
    fun useRefreshToken_shouldReturnNewTokens_withValidRefreshTokenAndValidAccessToken_200() {
        // Prepare Session
        val expiredAuthenticationDto = restTestSessionHelper.getValidAuthenticationResponseDtoForTestUser()

        // Check that session is not expired
        this.mockMvc.perform(
            MockMvcRequestBuilders.get("$USER_BASE_PATH$POST__CREATE_USER_PATH")
                .header(
                    HttpHeaders.AUTHORIZATION,
                    RestTestSessionHelper.getAuthenticationHeader(expiredAuthenticationDto.accessToken)
                )
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)


        // Renew authentication token
        val requestDto =
            AuthenticationTokenRefreshRequestDto(
                expiredAuthenticationDto.accessToken,
                expiredAuthenticationDto.refreshToken
            )

        val contentAsString = this.mockMvc.perform(
            MockMvcRequestBuilders.post("$AUTHENTICATION_BASE_PATH$POST__USE_REFRESH_TOKEN_PATH")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(requestDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsString

        val newAuthenticationDto =
            jacksonObjectMapper().readValue(contentAsString, AuthenticationResponseDto::class.java)


        // Check that new session is also valid
        this.mockMvc.perform(
            MockMvcRequestBuilders.get("$USER_BASE_PATH$POST__CREATE_USER_PATH")
                .header(
                    HttpHeaders.AUTHORIZATION,
                    RestTestSessionHelper.getAuthenticationHeader(newAuthenticationDto.accessToken)
                )
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)

    }

    @Test
    fun useRefreshToken_shouldReturnNewTokens_withValidRefreshTokenAndExpiredAccessToken_200() {
        val testClock: TestClock = clock as TestClock
        // Prepare Session
        val expiredAuthenticationDto = restTestSessionHelper.getValidAuthenticationResponseDtoForTestUser()

        testClock.fastForward(JWT_ACCESS_TOKEN_VALIDITY_DURATION.multipliedBy(2)) // jump in time and invalidate auth token

        // Check that session is really expired
        this.mockMvc.perform(
            MockMvcRequestBuilders.get("$USER_BASE_PATH$POST__CREATE_USER_PATH")
                .header(
                    HttpHeaders.AUTHORIZATION,
                    RestTestSessionHelper.getAuthenticationHeader(expiredAuthenticationDto.accessToken)
                )
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)


        // Renew authentication token
        val requestDto =
            AuthenticationTokenRefreshRequestDto(
                expiredAuthenticationDto.accessToken,
                expiredAuthenticationDto.refreshToken
            )

        val contentAsString = this.mockMvc.perform(
            MockMvcRequestBuilders.post("$AUTHENTICATION_BASE_PATH$POST__USE_REFRESH_TOKEN_PATH")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(requestDto))
        ).andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsString

        val newAuthenticationDto =
            jacksonObjectMapper().readValue(contentAsString, AuthenticationResponseDto::class.java)


        // Check that new session is not expired anymore
        this.mockMvc.perform(
            MockMvcRequestBuilders.get("$USER_BASE_PATH$POST__CREATE_USER_PATH")
                .header(
                    HttpHeaders.AUTHORIZATION,
                    RestTestSessionHelper.getAuthenticationHeader(newAuthenticationDto.accessToken)
                )
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)

    }

    @Test
    fun useRefreshToken_shouldFail_ifRefreshTokenIsExpired_401() {
        val testClock: TestClock = clock as TestClock
        // Prepare Session
        val expiredAuthenticationDto = restTestSessionHelper.getValidAuthenticationResponseDtoForTestUser()

        testClock.fastForward(JWT_REFRESH_TOKEN_VALIDITY_DURATION.multipliedBy(2)) // jump in time and invalidate auth token

        // Renew authentication token
        val requestDto =
            AuthenticationTokenRefreshRequestDto(
                expiredAuthenticationDto.accessToken,
                expiredAuthenticationDto.refreshToken
            )

        this.mockMvc.perform(
            MockMvcRequestBuilders.post("$AUTHENTICATION_BASE_PATH$POST__USE_REFRESH_TOKEN_PATH")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(requestDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun useRefreshToken_shouldFail_ifInvalidRefreshToken_422() {
        val requestDto =
            AuthenticationTokenRefreshRequestDto(
                "notWorkingAccessToken",
                "notWorkingRefreshToken"
            )

        this.mockMvc.perform(
            MockMvcRequestBuilders.post("$AUTHENTICATION_BASE_PATH$POST__USE_REFRESH_TOKEN_PATH")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(requestDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    fun useRefreshToken_shouldFail_ifInvalidPreviousAccessToken_422() {
        val authenticationDto = restTestSessionHelper.getValidAuthenticationResponseDtoForTestUser()

        val requestDto =
            AuthenticationTokenRefreshRequestDto(
                "notWorkingAccessToken",
                authenticationDto.refreshToken
            )

        this.mockMvc.perform(
            MockMvcRequestBuilders.post("$AUTHENTICATION_BASE_PATH$POST__USE_REFRESH_TOKEN_PATH")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(requestDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    fun useRefreshToken_shouldFail_ifAccessTokenDoesNotMatchAccessToken_401() {
        val authenticationDto1 = restTestSessionHelper.getValidAuthenticationResponseDtoForTestUser("TestUser1")
        val authenticationDto2 = restTestSessionHelper.getValidAuthenticationResponseDtoForTestUser("TestUser2")

        val requestDto =
            AuthenticationTokenRefreshRequestDto(
                authenticationDto1.accessToken,
                authenticationDto2.refreshToken
            )

        this.mockMvc.perform(
            MockMvcRequestBuilders.post("$AUTHENTICATION_BASE_PATH$POST__USE_REFRESH_TOKEN_PATH")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(requestDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun listGrantedRefreshTokens_shouldFail_ifNotAuthenticated_401() {
        this.mockMvc.perform(
            MockMvcRequestBuilders.get("$AUTHENTICATION_BASE_PATH$GET__LIST_GRANTED_REFRESH_TOKENS")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun listGrantedRefreshTokens_shouldReturnList_ifAuthenticated_200() {
        val jwtAuthenticationHeader = RestTestSessionHelper.getAuthenticationHeader(
            restTestSessionHelper.getValidAuthenticationResponseDtoForTestUser().accessToken
        )

        val contentAsString = this.mockMvc.perform(
            MockMvcRequestBuilders.get("$AUTHENTICATION_BASE_PATH$GET__LIST_GRANTED_REFRESH_TOKENS")
                .header(HttpHeaders.AUTHORIZATION, jwtAuthenticationHeader)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsString


        val mapper = jacksonObjectMapper()
        mapper.registerModule(JavaTimeModule())

        val collectionType: CollectionType = mapper.typeFactory
            .constructCollectionType(List::class.java, GrantedRefreshTokenDTO::class.java)

        val tokens: List<GrantedRefreshTokenDTO> = mapper.readValue(contentAsString, collectionType)

        assertFalse(tokens.isEmpty())
        assertEquals(1, tokens.size)
    }
}
