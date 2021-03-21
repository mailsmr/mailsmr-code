package io.mailsmr.domain.events

import org.springframework.context.ApplicationEvent

abstract class WebSocketPathConnectionEvent(
    source: Any,

    val sessionId: String,
    val simpPath: String,
) : ApplicationEvent(source)
