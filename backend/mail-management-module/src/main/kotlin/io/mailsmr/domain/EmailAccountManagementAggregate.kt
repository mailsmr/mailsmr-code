package io.mailsmr.domain

import io.mailsmr.infrastructure.exceptions.NoConnectionEstablishedException
import io.mailsmr.infrastructure.imap.IMAPEmailAccount
import io.mailsmr.infrastructure.imap.IMAPEmailAccountContext
import io.mailsmr.infrastructure.imap.IMAPSession
import java.time.Duration
import java.util.concurrent.ScheduledExecutorService
import java.util.stream.Collectors

class EmailAccountManagementAggregate(
    private val emailAccountConnectionProperties: EmailAccountConnectionProperties,
    private val scheduledExecutorService: ScheduledExecutorService,
    private val checkFrequencyIfIdleNotSupported: Duration = Duration.ofSeconds(15)
) {
    private var _imapEmailAccount: IMAPEmailAccount? = null
    private var imapEmailAccount: IMAPEmailAccount
        get() {
            if (_imapEmailAccount == null) {
                throw NoConnectionEstablishedException("An IMAP connection needs to be opened with the decryption password before accessing the account.")
            }
            return _imapEmailAccount!!
        }
        private set(value) {
            _imapEmailAccount = value
        }

    fun connectImap(decryptionPassword: String) {
        val imapSession = IMAPSession(emailAccountConnectionProperties.imap, decryptionPassword)

        imapEmailAccount = IMAPEmailAccount.getInstanceForEmailAddress(
            IMAPEmailAccountContext(
                emailAccountConnectionProperties.imap.emailAddress,
                imapSession.nativeImapStore,
                imapSession.getNewIdleManager(scheduledExecutorService, checkFrequencyIfIdleNotSupported)
            )
        )
    }

    fun getFolder(folderName: String): EmailFolder {
        val imapFolder = imapEmailAccount.getFolder(folderName)

        return EmailFolder(imapFolder)
    }

    fun getFolders(): List<EmailFolder> {
        return imapEmailAccount.getFolders().parallelStream()
            .map(::EmailFolder)
            .collect(Collectors.toList())
    }
}
