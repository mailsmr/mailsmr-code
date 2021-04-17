package io.mailsmr.infrastructure.exceptions

import jakarta.mail.MessagingException
import mu.KotlinLogging


class MessagingRuntimeException(e: MessagingException) : RuntimeException(e) {
    private val logger = KotlinLogging.logger {}

    init {
        logger.error(e.message, e)
    }
}
