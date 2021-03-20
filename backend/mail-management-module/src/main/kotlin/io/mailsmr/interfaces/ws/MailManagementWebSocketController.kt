package io.mailsmr.interfaces.ws

import io.mailsmr.application.WebSocketMessagingHelper
import io.mailsmr.domain.events.WebSocketExistingUserPathConnectionEvent
import io.mailsmr.domain.events.WebSocketUserPathConnectionClosedEvent
import io.mailsmr.domain.events.WebSocketNewUserPathConnectionEvent
import io.mailsmr.interfaces.ws.dtos.WebSocketEmailsResponseDto
import org.springframework.context.event.EventListener
import org.springframework.messaging.Message
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller


@Controller
class MailManagementWebSocketController(
    private val webSocketMessagingHelper: WebSocketMessagingHelper,
    private val messagingTemplate: SimpMessagingTemplate
) {
    @MessageMapping("/hello")
    @SendToUser("/queue/messages")
    fun send(
        @Payload message: Message<String>,
        @Header("simpSessionId") sessionId: String,
        principal: java.security.Principal
    ): WebSocketEmailsResponseDto {
        Thread.sleep(1000)
        return WebSocketEmailsResponseDto(folderName = principal.name)
    }

    @EventListener
    fun handleUserMessagesNewConnection(event: WebSocketNewUserPathConnectionEvent) { // TODO rename
        if (event.simpPath != "/user/queue/messages") return

        println("=== OPEN CONNECTION AND SEND DATA (U): " + event.simpPath)

        webSocketMessagingHelper.convertAndSendToUser(
            event.userPrincipal.name,
            event.simpPath,
            "=== OPEN CONNECTION AND SEND DATA: " + event.simpPath
        )

        webSocketMessagingHelper.convertAndSendToSession(
            event.sessionId,
            event.simpPath,
            "=== OPEN CONNECTION AND SEND DATA (S): " + event.simpPath
        )
    }


    @EventListener
    fun handleUserMessagesConnection(event: WebSocketExistingUserPathConnectionEvent) { // TODO rename
        if (event.simpPath != "/user/queue/messages") return

        println("=== RESEND DATA: " + event.simpPath)

        webSocketMessagingHelper.convertAndSendToUser(
            event.userPrincipal.name,
            event.simpPath,
            "=== RESEND DATA (U): " + event.simpPath
        )

        webSocketMessagingHelper.convertAndSendToSession(
            event.sessionId,
            event.simpPath,
            "=== RESEND DATA (S): " + event.simpPath
        )
    }

    @EventListener
    fun handleUserMessagesConnection(event: WebSocketUserPathConnectionClosedEvent) { // TODO rename
        if (event.simpPath != "/user/queue/messages") return

        println("=== CLOSE CONNECTION: " + event.simpPath)
    }
}
