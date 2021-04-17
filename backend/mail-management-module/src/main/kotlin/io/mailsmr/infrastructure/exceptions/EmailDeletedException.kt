package io.mailsmr.infrastructure.exceptions

import mu.KotlinLogging

class EmailDeletedException(messageId: Long) : RuntimeException("Email with id \"$messageId\" is deleted.") {
    private val logger = KotlinLogging.logger {}

    init {
        logger.warn("Email with id \"$messageId\" is deleted.")
    }
}
