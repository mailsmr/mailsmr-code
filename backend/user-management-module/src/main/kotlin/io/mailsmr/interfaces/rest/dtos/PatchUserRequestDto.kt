package io.mailsmr.interfaces.rest.dtos

data class PatchUserRequestDto(
        val username: String? = null,
        val recoveryEmailAddress: String? = null,
        val passwordChange: PatchUserRequestDtoPasswordChange? = null
) {
    data class PatchUserRequestDtoPasswordChange(
            val previousPassword: String,
            val newPassword: String
    )
}
