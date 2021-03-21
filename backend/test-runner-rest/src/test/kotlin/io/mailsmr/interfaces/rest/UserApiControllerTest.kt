package io.mailsmr.interfaces.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mailsmr.helpers.RestTestSessionHelper
import io.mailsmr.interfaces.rest.UserApiPaths.DELETE__DELETE_USER_PATH
import io.mailsmr.interfaces.rest.UserApiPaths.GET__GET_USER_PATH
import io.mailsmr.interfaces.rest.UserApiPaths.PATCH__PATCH_USER_PATH
import io.mailsmr.interfaces.rest.UserApiPaths.POST__CREATE_USER_PATH
import io.mailsmr.interfaces.rest.UserApiPaths.USER_BASE_PATH
import io.mailsmr.interfaces.rest.dtos.NewUserRequestDto
import io.mailsmr.interfaces.rest.dtos.PatchUserRequestDto
import io.mailsmr.interfaces.rest.dtos.UserDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import javax.transaction.Transactional


@Transactional
@AutoConfigureMockMvc
@SpringBootTest
internal class UserApiControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var restTestSessionHelper: RestTestSessionHelper

    @Test
    fun createUser_validRequest_201() {
        val newUserRequestDto = NewUserRequestDto("TestUser", "12345678", "test@test.com")

        this.mockMvc.perform(
            post("$USER_BASE_PATH$POST__CREATE_USER_PATH")
                .contentType(APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(newUserRequestDto))
        )
            .andDo(print())
            .andExpect(status().isCreated)
    }

    @Test
    fun createUser_duplicateUser_409() {
        val newUserRequestDto = NewUserRequestDto("TestUser", "12345678", "test@test.com")

        this.mockMvc.perform(
            post("$USER_BASE_PATH$POST__CREATE_USER_PATH")
                .contentType(APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(newUserRequestDto))
        )

        this.mockMvc.perform(
            post("$USER_BASE_PATH$POST__CREATE_USER_PATH")
                .contentType(APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(newUserRequestDto))
        )
            .andDo(print())
            .andExpect(status().isConflict)
    }

    @Test
    fun getUser_valid_ifAuthenticated_200() {
        val jwtAuthenticationHeader = RestTestSessionHelper.getAuthenticationHeader(
            restTestSessionHelper.getValidAuthenticationResponseDtoForTestUser().accessToken
        )

        this.mockMvc.perform(
            get("$USER_BASE_PATH$GET__GET_USER_PATH").header(HttpHeaders.AUTHORIZATION, jwtAuthenticationHeader)
        )
            .andDo(print())
            .andExpect(status().isOk)
    }

    @Test
    fun getUser_fail_ifNotAuthenticated_401() {
        this.mockMvc.perform(
            get("$USER_BASE_PATH$GET__GET_USER_PATH")
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
    }


    @Test
    fun deleteUser_valid_ifAuthenticated_204() {
        val jwtAuthenticationHeader = RestTestSessionHelper.getAuthenticationHeader(
            restTestSessionHelper.getValidAuthenticationResponseDtoForTestUser().accessToken
        )

        this.mockMvc.perform(
            delete("$USER_BASE_PATH$DELETE__DELETE_USER_PATH").header(
                HttpHeaders.AUTHORIZATION,
                jwtAuthenticationHeader
            )
        )
            .andDo(print())
            .andExpect(status().isNoContent)
    }

    @Test
    fun deleteUser_fail_ifNotAuthenticated_401() {
        this.mockMvc.perform(
            get("$USER_BASE_PATH$DELETE__DELETE_USER_PATH")
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
    }


    @Test
    fun patchUser_valid_patchOnlyUsername_ifAuthenticated_200() {
        val jwtAuthenticationHeader = RestTestSessionHelper.getAuthenticationHeader(
            restTestSessionHelper.getValidAuthenticationResponseDtoForTestUser().accessToken
        )

        val patchUserRequestDto = PatchUserRequestDto(username = "NewTestUser")

        val contentAsString = this.mockMvc.perform(
            patch("$USER_BASE_PATH$PATCH__PATCH_USER_PATH")
                .header(HttpHeaders.AUTHORIZATION, jwtAuthenticationHeader)
                .contentType(APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(patchUserRequestDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val userDto = jacksonObjectMapper().readValue(contentAsString, UserDto::class.java)

        assertEquals(userDto.username, patchUserRequestDto.username)
    }

    @Test
    fun patchUser_valid_patchOnlyRecoveryEmailAddress_ifAuthenticated_200() {
        val jwtAuthenticationHeader = RestTestSessionHelper.getAuthenticationHeader(
            restTestSessionHelper.getValidAuthenticationResponseDtoForTestUser().accessToken
        )

        val patchUserRequestDto = PatchUserRequestDto(recoveryEmailAddress = "test2@test2.com")

        val contentAsString = this.mockMvc.perform(
            patch("$USER_BASE_PATH$PATCH__PATCH_USER_PATH")
                .header(HttpHeaders.AUTHORIZATION, jwtAuthenticationHeader)
                .contentType(APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(patchUserRequestDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val userDto = jacksonObjectMapper().readValue(contentAsString, UserDto::class.java)

        assertEquals(userDto.recoveryEmail, patchUserRequestDto.recoveryEmailAddress)
    }

    @Test
    fun patchUser_valid_patchOnlyPassword_ifAuthenticated_200() {
        val jwtAuthenticationHeader = RestTestSessionHelper.getAuthenticationHeader(
            restTestSessionHelper.getValidAuthenticationResponseDtoForTestUser().accessToken
        )

        val patchUserRequestDto = PatchUserRequestDto(
            passwordChange = PatchUserRequestDto.PatchUserRequestDtoPasswordChange(
                RestTestSessionHelper.PASSWORD,
                "987654321"
            )
        )

        this.mockMvc.perform(
            patch("$USER_BASE_PATH$PATCH__PATCH_USER_PATH")
                .header(HttpHeaders.AUTHORIZATION, jwtAuthenticationHeader)
                .contentType(APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(patchUserRequestDto))
        )
            .andDo(print())
            .andExpect(status().isOk)

        this.mockMvc.perform(
            patch("$USER_BASE_PATH$PATCH__PATCH_USER_PATH")
                .header(HttpHeaders.AUTHORIZATION, jwtAuthenticationHeader)
                .contentType(APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(patchUserRequestDto))
        )
            .andDo(print())
            .andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun patchUser_fail_ifNotAuthenticated_401() {
        this.mockMvc.perform(
            get("$USER_BASE_PATH$PATCH__PATCH_USER_PATH")
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
    }

}
