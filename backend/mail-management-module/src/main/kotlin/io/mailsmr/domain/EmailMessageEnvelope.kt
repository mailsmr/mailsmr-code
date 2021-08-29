package io.mailsmr.domain

import io.mailsmr.infrastructure.imap.IMAPEmailEnvelope
import java.util.*

data class EmailMessageEnvelope(
    val uid: Long,
    val messageId: String,
    val from: List<String>? = null,
    val to: List<String>? = null,
    val cc: List<String>? = null,
    val bcc: List<String>? = null,
    val newsgroup: List<String>? = null,
    val replyTo: List<String>? = null,
    val sentDate: Date? = null,
    val receivedDate: Date? = null,
    val subject: String? = null,
    val folder: String,
    val expunged: Boolean,
    val answered: Boolean? = null,
    val deleted: Boolean? = null,
    val draft: Boolean? = null,
    val flagged: Boolean? = null,
    val recent: Boolean? = null,
    val seen: Boolean? = null,
    val receiverAccountEmailAddress: String,
    val contentPeek: String? = null,
) {
    companion object {
        internal fun fromIMAPEmailEnvelope(originalEnvelope: IMAPEmailEnvelope): EmailMessageEnvelope {
            return EmailMessageEnvelope(
                uid = originalEnvelope.uid,
                messageId = originalEnvelope.messageId,
                from = originalEnvelope.from,
                to = originalEnvelope.to,
                cc = originalEnvelope.cc,
                bcc = originalEnvelope.bcc,
                newsgroup = originalEnvelope.newsgroup,
                replyTo = originalEnvelope.replyTo,
                sentDate = originalEnvelope.sentDate,
                receivedDate = originalEnvelope.receivedDate,
                subject = originalEnvelope.subject,
                expunged = originalEnvelope.expunged,
                folder = originalEnvelope.folder,
                answered = originalEnvelope.answered,
                deleted = originalEnvelope.deleted,
                draft = originalEnvelope.draft,
                flagged = originalEnvelope.flagged,
                recent = originalEnvelope.recent,
                seen = originalEnvelope.seen,
                receiverAccountEmailAddress = originalEnvelope.receiverAccountEmailAddress,
                contentPeek = originalEnvelope.contentPeek,
            )
        }
    }
}
