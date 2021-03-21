package io.mailsmr.interfaces.rest.dtos

data class NewUserRequestDto(
    val username: String,
    val password: String,
    val recoveryEmailAddress: String,
)
