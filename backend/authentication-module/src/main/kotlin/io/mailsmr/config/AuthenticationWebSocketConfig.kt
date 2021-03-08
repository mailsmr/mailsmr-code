package io.mailsmr.config

import io.mailsmr.application.AuthenticationUserDetailsService
import io.mailsmr.domain.JwtTokenFactory
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99) // interceptor config must be before spring security
@EnableWebSocketMessageBroker
class AuthenticationWebSocketConfig(
    private val authenticationUserDetailsService: AuthenticationUserDetailsService,
    private val jwtTokenFactory: JwtTokenFactory
) : WebSocketMessageBrokerConfigurer {

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(object : ChannelInterceptor {
            override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
                val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)

                when (accessor!!.command) {
                    StompCommand.CONNECT -> authenticateUserAndSetPrincipal(accessor)
                    StompCommand.SUBSCRIBE, StompCommand.MESSAGE, StompCommand.SEND ->
                        if (accessor.user == null) authenticateUserAndSetPrincipal(accessor)
                    else -> {
                        /*stub*/
                    }
                }
                return message
            }
        })
    }

    private fun authenticateUserAndSetPrincipal(accessor: StompHeaderAccessor) {
        val accessToken = jwtTokenFactory.fromStompHeaderAccessor(accessor)

        val abstractAuthenticationToken =
            authenticationUserDetailsService.getAuthenticationTokenFromAccessToken(accessToken)

        if (abstractAuthenticationToken != null) {
            accessor.user = abstractAuthenticationToken
        }
    }
}

