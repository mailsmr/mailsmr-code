package io.mailsmr.interfaces.rest.dtos

import io.mailsmr.common.infrastructure.entities.User


data class UserDto(
        val id: Long,
        val username: String,
        val recoveryEmail: String,
) {
    companion object {
        fun fromUser(user: User): UserDto = UserDto(
                user.id,
                user.username,
                user.recoveryEmailAddress
        )
    }
}
