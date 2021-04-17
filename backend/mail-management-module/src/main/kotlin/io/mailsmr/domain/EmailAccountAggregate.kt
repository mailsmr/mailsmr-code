package io.mailsmr.domain

import io.mailsmr.infrastructure.exceptions.NoConnectionEstablishedException
import io.mailsmr.infrastructure.imap.IMAPEmailAccount
import io.mailsmr.infrastructure.imap.IMAPSession
import java.util.stream.Collectors

class EmailAccountAggregate(
    private val emailAccountConnectionProperties: EmailAccountConnectionProperties
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


//    private var _smtpEmailAccount: SMTPEmailAccount? = null
//    private var smtpEmailAccount: SMTPEmailAccount
//        get() {
//            if (_smtpEmailAccount == null) {
//                throw NoConnectionEstablishedException("An SMTP connection needs to be opened with the decryption password before accessing the account.")
//            }
//            return _smtpEmailAccount!!
//        }
//        private set(value) {
//            _smtpEmailAccount = value
//        }


    fun connectSmtp(decryptionPassword: String) {
        TODO()
    }

    fun connectImap(decryptionPassword: String) {
        val imapSession = IMAPSession(emailAccountConnectionProperties.imap, decryptionPassword)

        imapEmailAccount = IMAPEmailAccount.getInstanceForEmailAddress(
            emailAccountConnectionProperties.imap.emailAddress,
            imapSession.nativeImapStore
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

    fun sendMessage() {
        TODO()
    }
}
