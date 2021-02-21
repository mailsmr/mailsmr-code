package io.mailsmr.interfaces.rest.dtos

import java.time.LocalDateTime

data class GrantedRefreshTokenDTO (
    val id: String,
    val description: String? = null,
    val expirationDateTime: LocalDateTime,
    val creationDateTime: LocalDateTime,
)
