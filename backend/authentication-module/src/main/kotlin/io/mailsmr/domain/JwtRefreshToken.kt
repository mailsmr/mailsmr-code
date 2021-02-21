package io.mailsmr.domain

import java.time.Clock

class JwtRefreshToken(
    jwtTokenString: String,
    jwtTokenSecret: String,
    clock: Clock
) : JwtToken(jwtTokenString, jwtTokenSecret, clock) {
    override fun isViable(): Boolean = super.isViable() && isRefreshToken()

    public override fun getJti(): String? = super.getJti()

    private fun isRefreshToken(): Boolean = isJtiSet()
}
