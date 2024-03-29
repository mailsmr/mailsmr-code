package io.mailsmr.infrastructure.imap

import com.sun.mail.imap.IMAPStore
import io.mailsmr.domain.EmailAccountConnectionProperties
import io.mailsmr.util.AESDecryptionUtil.decryptPassword
import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import java.time.Duration
import java.util.*
import java.util.concurrent.ScheduledExecutorService

internal class IMAPSession(
    imapConnectionProperties: EmailAccountConnectionProperties.IMAPConnectionProperties,
    decryptionPassword: String
) {
    val nativeImapStore: IMAPStore

    private val nativeImapSession: Session

    init {
        val decryptedImapPassword = decryptPassword(imapConnectionProperties.password, decryptionPassword)

        nativeImapSession = Session.getInstance(
            getConnectionProperties(imapConnectionProperties),
            object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(
                        imapConnectionProperties.username,
                        decryptedImapPassword
                    )
                }
            })

        nativeImapStore = nativeImapSession.getStore(imapConnectionProperties.protocol.value) as IMAPStore
        nativeImapStore.connect(
            imapConnectionProperties.address, imapConnectionProperties.port,
            imapConnectionProperties.username, decryptedImapPassword
        )
    }

    companion object {
        private fun getConnectionProperties(imapConnectionProperties: EmailAccountConnectionProperties.IMAPConnectionProperties): Properties {
            // https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html
            val properties = Properties()

            // TODO verify if those settings are correct and universally applicable
            properties["mail.imap.ssl.trust"] = "*"
            properties["mail.imap.starttls.enable"] = true
            properties["mail.imap.auth"] = true
            properties["mail.imap.usesocketchannels"] = true
            properties["mail.imaps.ssl.trust"] = "*"
            properties["mail.imaps.starttls.enable"] = true
            properties["mail.imaps.auth"] = true
            properties["mail.imaps.usesocketchannels"] = true

            return properties
        }
    }

    fun getNewIdleManager(scheduledExecutorService: ScheduledExecutorService, checkFrequencyIfIdleNotSupported: Duration): IMAPIdleManager {
        return IMAPIdleManager(nativeImapSession, scheduledExecutorService, checkFrequencyIfIdleNotSupported)
    }

}
