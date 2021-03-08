package io.mailsmr.domain.events

import org.springframework.context.ApplicationEvent
import java.security.Principal

abstract class WebSocketPathConnectionEvent(
    source: Any,

    val sessionId: String,
    val simpPath: String,
) : ApplicationEvent(source)
