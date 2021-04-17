package io.mailsmr.domain

import io.mailsmr.infrastructure.exceptions.EmailDeletedException
import io.mailsmr.infrastructure.imap.IMAPFolder
import io.mailsmr.infrastructure.imap.IMAPMessage

class EmailMessage internal constructor(
    private val imapFolder: IMAPFolder,
    private val imapMessage: IMAPMessage
) {
    private var deleted: Boolean = false

    fun getSubject(): String = imapMessage.nativeMessage.subject

    fun getContent(): String = imapMessage.textFromMessage

    fun copyTo() {
        TODO()
    }

    fun moveTo() {
        TODO()
    }

    fun delete(): Boolean {
        val messageId = imapMessage.messageId.toLong()
        if (deleted) throw EmailDeletedException(messageId)
        return imapFolder.deleteMail(messageId)
    }
}
