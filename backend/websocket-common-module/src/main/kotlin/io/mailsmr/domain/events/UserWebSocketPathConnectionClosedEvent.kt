package io.mailsmr.domain.events

import java.security.Principal

class UserWebSocketPathConnectionClosedEvent(
    source: Any,
    userPrincipal: Principal,
    sessionId: String,
    simpPath: String,
) : UserWebSocketPathConnectionEvent(source, userPrincipal, sessionId, simpPath)
