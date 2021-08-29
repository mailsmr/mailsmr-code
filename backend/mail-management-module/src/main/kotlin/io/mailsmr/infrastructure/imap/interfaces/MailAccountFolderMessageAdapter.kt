package io.mailsmr.infrastructure.imap.interfaces

import io.mailsmr.infrastructure.imap.IMAPEmailEnvelope

/**
 * The adapter which receives MailAccountFolderNewMessage events.
 * The methods in this class are empty;  this class is provided as a
 * convenience for easily creating listeners by extending this class
 * and overriding only the methods of interest.
 */
internal open class MailAccountFolderMessageAdapter : MailAccountFolderMessageListener {
    override fun messagesAdded(messages: List<IMAPEmailEnvelope>) {
        // stub
    }

    override fun messagesRemoved(messages: List<IMAPEmailEnvelope>) {
        // stub
    }

    override fun messageChanged(message: IMAPEmailEnvelope) {
        // stub
    }
}
