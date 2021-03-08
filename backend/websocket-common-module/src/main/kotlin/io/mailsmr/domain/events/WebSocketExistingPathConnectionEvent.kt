package io.mailsmr.domain.events

open class WebSocketExistingPathConnectionEvent(source: Any, sessionId: String, simpPath: String) :
    WebSocketPathConnectionEvent(source, sessionId, simpPath)
