package io.mailsmr.domain

import io.mailsmr.infrastructure.protocols.IMAPProtocols
import io.mailsmr.infrastructure.protocols.SMTPProtocols

data class EmailAccountConnectionProperties(
    val imap: IMAPConnectionProperties,
    val smtp: SMTPConnectionProperties,
) {
    data class IMAPConnectionProperties(
        val protocol: IMAPProtocols,
        val address: String,
        val port: Int,

        val emailAddress: String,
        val username: String,
        val password: String,
    )

    data class SMTPConnectionProperties(
        val protocol: SMTPProtocols,
        val address: String,
        val port: Int,

        val emailAddress: String,
        val username: String,
        val password: String,
    )
}
