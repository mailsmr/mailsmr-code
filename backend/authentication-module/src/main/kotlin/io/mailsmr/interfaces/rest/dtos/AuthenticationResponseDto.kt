package io.mailsmr.interfaces.rest.dtos

data class AuthenticationResponseDto(
    var accessToken: String,
    val refreshToken: String,
)
