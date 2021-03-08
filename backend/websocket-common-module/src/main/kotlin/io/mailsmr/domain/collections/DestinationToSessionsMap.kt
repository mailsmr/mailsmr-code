package io.mailsmr.domain.collections

import java.util.concurrent.ConcurrentHashMap

internal class DestinationToSessionsMap {
    private val destinationToSessionsMap: ConcurrentHashMap<String, MutableSet<String>> = ConcurrentHashMap()

    fun allSessionsForDestinationAreClosed(destination: String): Boolean = getDestinationSessionsCount(destination) == 0

    fun isNewDestination(destination: String): Boolean {
        return !destinationToSessionsMap.containsKey(destination)
    }

    fun mapSessionToDestination(destination: String, sessionId: String) {
        destinationToSessionsMap.computeIfAbsent(destination) { HashSet() }.add(sessionId)
    }

    fun unmapSessionFromDestination(destination: String, sessionId: String) {
        val sessions = destinationToSessionsMap[destination]

        if (sessions != null) {
            sessions.remove(sessionId)

            if (sessions.isEmpty()) {
                destinationToSessionsMap.remove(destination)
            }
        }
    }

    private fun getDestinationSessionsCount(destination: String): Int = destinationToSessionsMap[destination]?.size ?: 0


}
