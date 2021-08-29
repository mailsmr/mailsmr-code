package io.mailsmr.infrastructure.protocols

import com.sun.mail.imap.IMAPProvider
import com.sun.mail.imap.IMAPSSLProvider
import jakarta.mail.Provider

enum class IMAPProtocols(val value: Provider) {
    IMAP(IMAPProvider()),
    IMAPS(IMAPSSLProvider())
}
