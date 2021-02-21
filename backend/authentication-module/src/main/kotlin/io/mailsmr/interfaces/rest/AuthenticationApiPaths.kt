package io.mailsmr.interfaces.rest

object AuthenticationApiPaths {
    const val AUTHENTICATION_BASE_PATH = "/authentication"

    const val POST__CREATE_AUTHENTICATION_TOKEN_PATH = "/api-token"
    const val POST__USE_REFRESH_TOKEN_PATH = "/api-token/refresh"
    const val GET__LIST_GRANTED_REFRESH_TOKENS = "/refresh-tokens"
}
