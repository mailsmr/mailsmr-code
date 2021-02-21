package io.mailsmr.interfaces.rest

import io.mailsmr.interfaces.rest.AuthenticationApiPaths.AUTHENTICATION_BASE_PATH
import io.mailsmr.interfaces.rest.AuthenticationApiPaths.GET__LIST_GRANTED_REFRESH_TOKENS
import io.mailsmr.interfaces.rest.AuthenticationApiPaths.POST__CREATE_AUTHENTICATION_TOKEN_PATH
import io.mailsmr.interfaces.rest.AuthenticationApiPaths.POST__USE_REFRESH_TOKEN_PATH
import io.mailsmr.interfaces.rest.dtos.AuthenticationResponseDto
import io.mailsmr.interfaces.rest.dtos.AuthenticationTokenRefreshRequestDto
import io.mailsmr.interfaces.rest.dtos.AuthenticationTokenRequestDto
import io.mailsmr.interfaces.rest.dtos.GrantedRefreshTokenDTO
import io.mailsmr.interfaces.rest.constants.OpenApiConfigConstants.SECURITY_SCHEME_NAME
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.HttpServletRequest

// TODO extra todo: @Validated/@Valid


@Tag(name = "authentication")
@RequestMapping(value = [AUTHENTICATION_BASE_PATH])
internal interface AuthenticationApi {
    @Operation(
        summary = "Authenticates the user and returns the authentication token",
        operationId = "apiToken"
    )
    @PostMapping(value = [POST__CREATE_AUTHENTICATION_TOKEN_PATH])
    fun createAuthenticationToken(
        @RequestBody authenticationRequestDto: AuthenticationTokenRequestDto,
        request: HttpServletRequest
    ): ResponseEntity<AuthenticationResponseDto>

    @Operation(
        summary = "Refreshes the authentication token with the help of the refresh token",
        operationId = "apiTokenRefresh"
    )
    @PostMapping(value = [POST__USE_REFRESH_TOKEN_PATH])
    fun useRefreshToken(
        @RequestBody authenticationTokenRefreshBearerDto: AuthenticationTokenRefreshRequestDto,
        request: HttpServletRequest
    ): ResponseEntity<AuthenticationResponseDto>


    @Operation(
        summary = "Returns list of all granted and valid refresh tokens",
        operationId = "listGrantedRefreshTokens",
        security = [SecurityRequirement(name = SECURITY_SCHEME_NAME)],
    )
    @GetMapping(value = [GET__LIST_GRANTED_REFRESH_TOKENS])
    fun listGrantedRefreshTokens(request: HttpServletRequest): ResponseEntity<Collection<GrantedRefreshTokenDTO>>
}
