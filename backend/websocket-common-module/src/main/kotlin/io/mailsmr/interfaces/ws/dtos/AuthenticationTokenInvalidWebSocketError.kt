package io.mailsmr.interfaces.ws.dtos

class AuthenticationTokenInvalidWebSocketError : UnauthorizedWebSocketError(
    "Authentication token is invalid or expired"
)
