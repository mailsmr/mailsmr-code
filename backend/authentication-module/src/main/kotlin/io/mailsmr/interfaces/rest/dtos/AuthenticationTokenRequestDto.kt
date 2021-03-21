package io.mailsmr.interfaces.rest.dtos

data class AuthenticationTokenRequestDto(
    var username: String,
    val password: String,
)
