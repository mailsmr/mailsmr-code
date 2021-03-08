package io.mailsmr.application

import io.mailsmr.domain.collections.DestinationToSessionsMap
import io.mailsmr.domain.collections.SessionToDestinationsMap
import io.mailsmr.domain.collections.UsernameToDestinationsMap
import io.mailsmr.domain.collections.UsernameToSessionsMap
import io.mailsmr.domain.events.*
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent
import java.security.Principal
import java.util.function.Consumer

@Service
internal class WebSocketUserConnectionHandler(
    private val applicationEventPublisher: ApplicationEventPublisher

) {
    private val usernameToSessionsMap: UsernameToSessionsMap = UsernameToSessionsMap()
    private val usernameToUserDestinationsMap: UsernameToDestinationsMap = UsernameToDestinationsMap()

    private val sessionToPublicDestinationsMap: SessionToDestinationsMap = SessionToDestinationsMap()
    private val publicDestinationToSessionsMap: DestinationToSessionsMap = DestinationToSessionsMap()


    @EventListener
    fun handleSessionSubscribed(event: SessionSubscribeEvent) {
        val destinationPath = event.message.headers["simpDestination"] as String

        if (isUserDestination(destinationPath)) {
            handleUserDestinationSubscribed(event)
        } else {
            handlePublicDestinationSubscribed(event)
        }
    }

    @EventListener
    fun handleSessionUnsubscribed(event: SessionUnsubscribeEvent) {
        val destinationPath = event.message.headers["simpDestination"] as String
        val sessionId = event.message.headers["simpSessionId"] as String
        val userPrincipal = event.user

        if (isUserDestination(destinationPath)) {
            handleUserDestinationSessionDisconnected(sessionId, userPrincipal, destinationPath)
        } else {
            handlePublicDestinationSessionDisconnected(sessionId, destinationPath)
        }
    }


    @EventListener
    fun handleSessionDisconnected(event: SessionDisconnectEvent) {
        val sessionId = event.message.headers["simpSessionId"] as String
        val userPrincipal = event.user

        if (userPrincipal != null) {
            handleUserSessionDisconnected(sessionId, userPrincipal)
        }
        handlePublicSessionDisconnected(sessionId)
    }

    private fun isUserDestination(destinationPath: String) = destinationPath.startsWith("/user")


    private fun handleUserDestinationSubscribed(event: SessionSubscribeEvent) {
        val sessionId = event.message.headers["simpSessionId"] as String
        val destinationPath = event.message.headers["simpDestination"] as String
        val userPrincipal = event.user ?: throw Exception("User principal is not set - user is not authenticated!")

        val username = userPrincipal.name

        usernameToSessionsMap.mapSessionToUser(sessionId, username)

        if (usernameToUserDestinationsMap.isNewDestinationForUser(username, destinationPath)) {
            val wsEvent = UserWebSocketNewPathConnectionEvent(this, userPrincipal, sessionId, destinationPath)
            applicationEventPublisher.publishEvent(wsEvent)
            usernameToUserDestinationsMap.mapDestinationToUser(username, destinationPath)
        } else {
            val wsEvent =
                UserWebSocketExistingPathConnectionEvent(this, userPrincipal, sessionId, destinationPath)
            applicationEventPublisher.publishEvent(wsEvent)
        }
    }

    private fun handlePublicDestinationSubscribed(event: SessionSubscribeEvent) {
        val sessionId = event.message.headers["simpSessionId"] as String
        val destinationPath = event.message.headers["simpDestination"] as String

        val wsEvent = if (publicDestinationToSessionsMap.isNewDestination(destinationPath)) {
            WebSocketNewPathConnectionEvent(this, sessionId, destinationPath)
        } else {
            WebSocketExistingPathConnectionEvent(this, sessionId, destinationPath)
        }
        publicDestinationToSessionsMap.mapSessionToDestination(destinationPath, sessionId)
        sessionToPublicDestinationsMap.mapDestinationToSession(sessionId, destinationPath)
        applicationEventPublisher.publishEvent(wsEvent)
    }

    private fun handleUserDestinationSessionDisconnected(
        sessionId: String,
        userPrincipal: Principal?,
        destinationPath: String
    ) {
        if (userPrincipal == null) throw Exception("User principal is not set - user is not authenticated!")

        val username = userPrincipal.name

        usernameToSessionsMap.unmapSessionFromUser(sessionId, username)

        if (usernameToSessionsMap.allSessionsForUserAreClosed(username)) {
            val wsEvent = UserWebSocketPathConnectionClosedEvent(this, userPrincipal, sessionId, destinationPath)
            applicationEventPublisher.publishEvent(wsEvent)
        }
    }

    private fun handlePublicSessionDisconnected(sessionId: String) {
        sessionToPublicDestinationsMap.clearSession(sessionId)?.forEach(Consumer { destination ->
            handlePublicDestinationSessionDisconnected(sessionId, destination)
        })
    }

    private fun handlePublicDestinationSessionDisconnected(sessionId: String, destinationPath: String) {
        publicDestinationToSessionsMap.unmapSessionFromDestination(destinationPath, sessionId)

        if (publicDestinationToSessionsMap.allSessionsForDestinationAreClosed(destinationPath)) {
            val wsEvent = WebSocketPathConnectionClosedEvent(this, sessionId, destinationPath)
            applicationEventPublisher.publishEvent(wsEvent)
        }
    }

    private fun handleUserSessionDisconnected(
        sessionId: String,
        userPrincipal: Principal,
    ) {
        val username = userPrincipal.name

        usernameToSessionsMap.unmapSessionFromUser(sessionId, username)
        if (usernameToSessionsMap.allSessionsForUserAreClosed(username)) {
            usernameToUserDestinationsMap.clearUser(username)?.forEach(Consumer { destination ->
                val wsEvent = UserWebSocketPathConnectionClosedEvent(this, userPrincipal, sessionId, destination)
                applicationEventPublisher.publishEvent(wsEvent)
            })
        }
    }

}
