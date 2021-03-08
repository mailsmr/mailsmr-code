package io.mailsmr.interfaces.ws.dtos

open class UnauthorizedWebSocketError(
    message: String?
) : WebSocketError(
    401,
    "UNAUTHORIZED",
    message
)
