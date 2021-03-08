package io.mailsmr.domain.events

open class WebSocketPathConnectionClosedEvent(source: Any, sessionId: String, simpPath: String) :
    WebSocketPathConnectionEvent(source, sessionId, simpPath)
