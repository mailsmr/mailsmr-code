package io.mailsmr.infrastructure.protocols

import com.sun.mail.smtp.SMTPProvider
import com.sun.mail.smtp.SMTPSSLProvider
import jakarta.mail.Provider

enum class SMTPProtocols(val value: Provider) {
    SMTP(SMTPProvider()),
    SMTPS(SMTPSSLProvider())
}
