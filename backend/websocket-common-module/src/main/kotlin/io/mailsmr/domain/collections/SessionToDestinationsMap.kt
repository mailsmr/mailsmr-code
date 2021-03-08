package io.mailsmr.domain.collections

import java.util.concurrent.ConcurrentHashMap

internal class SessionToDestinationsMap {
    private val usernameToDestinationsMap: ConcurrentHashMap<String, MutableSet<String>> = ConcurrentHashMap()

    fun clearSession(sessionId: String) = usernameToDestinationsMap.remove(sessionId)

    fun mapDestinationToSession(sessionId: String, destination: String) {
        usernameToDestinationsMap.computeIfAbsent(sessionId) { HashSet() }.add(destination)
    }
}
