package io.mailsmr.domain

import java.time.Clock

class JwtAccessToken(
    jwtTokenString: String,
    jwtTokenSecret: String,
    clock: Clock
) : JwtToken(jwtTokenString, jwtTokenSecret, clock) {
    override fun isViable(): Boolean = super.isViable() && isAccessToken()

    fun getEncryptedMasterPassword(): String? = getAllClaims()[ENCRYPTED_MASTER_PASSWORD_CLAIM]?.toString()

    private fun isAccessToken(): Boolean = getEncryptedMasterPassword() != null && getAllClaims()["jti"] == null
}
