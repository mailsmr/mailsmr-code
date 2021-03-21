package io.mailsmr.interfaces.rest.dtos

data class AuthenticationTokenRefreshRequestDto(
    var previousAccessToken: String,
    val refreshToken: String,
)
