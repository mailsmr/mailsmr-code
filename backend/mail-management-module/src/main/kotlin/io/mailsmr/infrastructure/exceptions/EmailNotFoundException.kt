package io.mailsmr.infrastructure.exceptions

import mu.KotlinLogging

class EmailNotFoundException(messageId: Long) : RuntimeException("Email with id \"$messageId\" not found.") {
    private val logger = KotlinLogging.logger {}

    init {
        logger.warn("Email with id \"$messageId\" not found.")
    }
}
