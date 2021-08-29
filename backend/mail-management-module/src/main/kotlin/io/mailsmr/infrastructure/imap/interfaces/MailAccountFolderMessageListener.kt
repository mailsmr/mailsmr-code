package io.mailsmr.infrastructure.imap.interfaces

import io.mailsmr.infrastructure.imap.IMAPEmailEnvelope

internal interface MailAccountFolderMessageListener {
    fun messagesAdded(messages: List<IMAPEmailEnvelope>)
    fun messagesRemoved(messages: List<IMAPEmailEnvelope>)
    fun messageChanged(message: IMAPEmailEnvelope)
}
