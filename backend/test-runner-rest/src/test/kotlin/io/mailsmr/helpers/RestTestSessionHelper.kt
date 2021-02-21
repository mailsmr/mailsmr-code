package io.mailsmr.helpers

import io.mailsmr.application.AuthenticationService
import io.mailsmr.interfaces.rest.dtos.AuthenticationResponseDto
import io.mailsmr.application.UserService
import org.springframework.stereotype.Service

@Service
internal class RestTestSessionHelper(
    private val userService: UserService,
    private val authenticationService: AuthenticationService
) {
    companion object {
        const val USERNAME = "TestUser"
        const val PASSWORD = "123456789"
        const val RECOVERY_EMAIL_ADDRESS = "test@test.com"

        fun getAuthenticationHeader(accessToken: String) = "Bearer $accessToken"
    }

    fun getValidAuthenticationResponseDtoForTestUser(
        username: String = USERNAME,
        password: String = PASSWORD,
        recoveryEmailAddress: String = RECOVERY_EMAIL_ADDRESS
    ): AuthenticationResponseDto {
        userService.createUser(username, password, recoveryEmailAddress)

        val (accessToken, refreshToken) = authenticationService.authenticateAndCreateJwtTokenPair(username, password)

        return AuthenticationResponseDto(accessToken.toString(), refreshToken.toString())
    }

}
