package io.mailsmr.interfaces.rest

import io.mailsmr.application.AuthenticationService
import io.mailsmr.interfaces.rest.dtos.AuthenticationResponseDto
import io.mailsmr.interfaces.rest.dtos.AuthenticationTokenRefreshRequestDto
import io.mailsmr.interfaces.rest.dtos.AuthenticationTokenRequestDto
import io.mailsmr.interfaces.rest.dtos.GrantedRefreshTokenDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest

@RestController
internal class AuthenticationApiController(
    private val authenticationService: AuthenticationService
) : AuthenticationApi {
    override fun createAuthenticationToken(
        @RequestBody authenticationRequestDto: AuthenticationTokenRequestDto,
        request: HttpServletRequest
    ): ResponseEntity<AuthenticationResponseDto> {
        val (username, password) = authenticationRequestDto

        val (accessToken, refreshToken) = authenticationService.authenticateAndCreateJwtTokenPair(username, password)

        val authenticationResponseDto = AuthenticationResponseDto(accessToken.toString(), refreshToken.toString())

        return ResponseEntity.ok(authenticationResponseDto)
    }

    override fun useRefreshToken(
        @RequestBody authenticationTokenRefreshBearerDto: AuthenticationTokenRefreshRequestDto,
        request: HttpServletRequest
    ): ResponseEntity<AuthenticationResponseDto> {
        val (previousAccessToken, currentRefreshToken) = authenticationTokenRefreshBearerDto
        val (newAccessToken, newRefreshToken) = authenticationService.useRefreshTokenAndGenerateNewJwtTokenPair(previousAccessToken, currentRefreshToken)

        val authenticationResponseDto = AuthenticationResponseDto(newAccessToken.toString(), newRefreshToken.toString())

        return ResponseEntity.ok(authenticationResponseDto)
    }

    override fun listGrantedRefreshTokens(request: HttpServletRequest): ResponseEntity<Collection<GrantedRefreshTokenDTO>> {
        val grantedRefreshTokensForUsername = authenticationService
            .getGrantedRefreshTokensForUsername(request.userPrincipal.name)
            .parallelStream()
            .map { t -> GrantedRefreshTokenDTO(t.jti, t.description, t.expirationDateTime, t.creationDateTime) }
            .collect(Collectors.toList())

        return ResponseEntity.ok(grantedRefreshTokensForUsername)
    }
}
