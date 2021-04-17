package io.mailsmr.domain

import io.mailsmr.infrastructure.imap.IMAPFolder
import java.util.stream.Collectors

class EmailFolder internal constructor(
    private val imapFolder: IMAPFolder
) {

    val totalMessageCount: Int get() = 0
    val unreadMessageCount: Int get() = 0
    val newMessageCount: Int get() = 0

    fun getMessage(id: Long): EmailMessage {
        val imapMessage = imapFolder.getMessage(id)
        return EmailMessage(imapFolder, imapMessage)
    }

    fun getMessages(): List<EmailMessage> {
        return imapFolder.messages.parallelStream()
            .map { message -> EmailMessage(imapFolder, message) }
            .collect(Collectors.toList())
    }

    fun subscribe() {
        TODO()
    }

    fun unsubscribe() {
        TODO()
    }
}
