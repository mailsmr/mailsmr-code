package io.mailsmr.domain

import io.mailsmr.infrastructure.enums.MessageCountType
import io.mailsmr.infrastructure.imap.IMAPEmailEnvelope
import io.mailsmr.infrastructure.imap.IMAPFolder
import io.mailsmr.infrastructure.imap.interfaces.MailAccountFolderMessageListener
import java.util.stream.Collectors

class EmailFolder internal constructor(
    private val imapFolder: IMAPFolder
) {

    val totalMessageCount: Int get() = imapFolder.getMessageCountByType(MessageCountType.ALL_MESSAGES)
    val unreadMessageCount: Int get() = imapFolder.getMessageCountByType(MessageCountType.UNREAD_MESSAGES)
    val newMessageCount: Int get() = imapFolder.getMessageCountByType(MessageCountType.NEW_MESSAGES)

    private val listenersMap: MutableMap<EmailFolderChangeListener, MailAccountFolderMessageListener> = mutableMapOf()

    fun getMessage(id: Long): EmailMessage {
        val imapMessage = imapFolder.getMessage(id)
        return EmailMessage(imapFolder, imapMessage)
    }

    fun getMessages(): List<EmailMessage> {
        return imapFolder.messages.parallelStream()
            .map { message -> EmailMessage(imapFolder, message) }
            .collect(Collectors.toList())
    }

    fun subscribe(listener: EmailFolderChangeListener) {
        val mailAccountFolderMessageListener = createListener(listener)
        listenersMap[listener] = mailAccountFolderMessageListener
        imapFolder.subscribe(mailAccountFolderMessageListener)
    }

    fun unsubscribe(listener: EmailFolderChangeListener) {
        if (listenersMap.containsKey(listener)) {
            imapFolder.disconnect(listenersMap[listener]!!)
        }
    }

    private fun createListener(listener: EmailFolderChangeListener): MailAccountFolderMessageListener {
        return (object : MailAccountFolderMessageListener {
            override fun messagesAdded(messages: List<IMAPEmailEnvelope>) {
                listener.any(messages) // TODO make right
            }

            override fun messagesRemoved(messages: List<IMAPEmailEnvelope>) {
                listener.any(messages) // TODO make right
            }

            override fun messageChanged(message: IMAPEmailEnvelope) {
                listener.any(message) // TODO make right
            }
        })
    }
}

