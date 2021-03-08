package io.mailsmr.domain.events

import java.security.Principal

abstract class UserWebSocketPathConnectionEvent(
    source: Any,
    val userPrincipal: Principal,
    sessionId: String,
    simpPath: String,
) : WebSocketPathConnectionEvent(source, sessionId, simpPath)
