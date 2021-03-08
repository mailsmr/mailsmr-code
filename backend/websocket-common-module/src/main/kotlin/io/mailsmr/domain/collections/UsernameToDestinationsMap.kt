package io.mailsmr.domain.collections

import java.util.concurrent.ConcurrentHashMap

internal class UsernameToDestinationsMap {
    private val usernameToDestinationsMap: ConcurrentHashMap<String, MutableSet<String>> = ConcurrentHashMap()

    fun clearUser(username: String) = usernameToDestinationsMap.remove(username)

    fun isNewDestinationForUser(username: String, destination: String): Boolean {
        return !getPathsForUser(username).contains(destination)
    }

    fun mapDestinationToUser(username: String, destination: String) {
        usernameToDestinationsMap.computeIfAbsent(username) { HashSet() }.add(destination)
    }

    private fun getPathsForUser(username: String) = usernameToDestinationsMap[username] ?: HashSet()
}
