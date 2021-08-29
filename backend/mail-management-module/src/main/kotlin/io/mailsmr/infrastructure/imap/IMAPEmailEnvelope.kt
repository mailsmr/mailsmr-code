package io.mailsmr.infrastructure.imap

import jakarta.mail.Address
import jakarta.mail.Flags
import jakarta.mail.Message.RecipientType.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage.RecipientType.NEWSGROUPS
import java.io.Serializable
import java.util.*
import java.util.stream.Collectors

internal data class IMAPEmailEnvelope(
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
) : Serializable, Comparable<IMAPEmailEnvelope> {

    override fun compareTo(other: IMAPEmailEnvelope): Int {
        return this.receivedDate?.compareTo(other.receivedDate) ?: 0
    }

    companion object {
        val NULL_INSTANCE = IMAPEmailEnvelope(
            uid = Long.MIN_VALUE,
            messageId = "NULL_INSTANCE",
            folder = "NULL_INSTANCE",
            expunged = false,
            receiverAccountEmailAddress = "NULL_INSTANCE"
        )

        fun fromMessage(message: IMAPMessage, accountEmailAddress: String): IMAPEmailEnvelope {
            return if (message.nativeMessage.isExpunged) {
                fromDeletedMessage(message, accountEmailAddress)
            } else {
                fromExistingMessage(message, accountEmailAddress)
            }
        }

        fun fromMessages(messages: List<IMAPMessage>, accountEmailAddress: String): List<IMAPEmailEnvelope> {
            return messages.stream().parallel()
                .map { message: IMAPMessage -> fromMessage(message, accountEmailAddress) }
                .collect(Collectors.toList())
        }

        private fun fromDeletedMessage(message: IMAPMessage, accountEmailAddress: String): IMAPEmailEnvelope {
            val nativeMessage = message.nativeMessage

            return IMAPEmailEnvelope(
                uid = message.uid,
                messageId = message.messageId,
                expunged = nativeMessage.isExpunged,
                folder = nativeMessage.folder.fullName,
                receiverAccountEmailAddress = accountEmailAddress
            )
        }

        private fun fromExistingMessage(message: IMAPMessage, accountEmailAddress: String): IMAPEmailEnvelope {
            val nativeMessage = message.nativeMessage
            return IMAPEmailEnvelope(
                message.uid,
                message.messageId,
                convertAddressArrayToStringList(nativeMessage.from ?: emptyArray()),
                convertAddressArrayToStringList(nativeMessage.getRecipients(TO) ?: emptyArray()),
                convertAddressArrayToStringList(nativeMessage.getRecipients(CC) ?: emptyArray()),
                convertAddressArrayToStringList(nativeMessage.getRecipients(BCC) ?: emptyArray()),
                convertAddressArrayToStringList(nativeMessage.getRecipients(NEWSGROUPS) ?: emptyArray()),
                convertAddressArrayToStringList(nativeMessage.replyTo ?: emptyArray()),
                nativeMessage.sentDate,
                nativeMessage.receivedDate,
                nativeMessage.subject,
                nativeMessage.folder.fullName,
                nativeMessage.isExpunged,
                nativeMessage.flags.contains(Flags.Flag.ANSWERED),
                nativeMessage.flags.contains(Flags.Flag.DELETED),
                nativeMessage.flags.contains(Flags.Flag.DRAFT),
                nativeMessage.flags.contains(Flags.Flag.FLAGGED),
                nativeMessage.flags.contains(Flags.Flag.RECENT),
                nativeMessage.flags.contains(Flags.Flag.SEEN),
                accountEmailAddress,
                message.contentPeek
            )
        }

        fun clone(originalEnvelope: IMAPEmailEnvelope): IMAPEmailEnvelope {
            return IMAPEmailEnvelope(
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

        private fun convertAddressArrayToStringList(addresses: Array<Address>): List<String> {
            return Arrays.stream(addresses)
                .parallel()
                .map { address ->
                    val internetAddress: InternetAddress = address as InternetAddress
                    val emailAddress: String = internetAddress.address
                    val personal: String? = internetAddress.personal

                    if (personal != null) {
                        return@map String.format("%s <%s>", personal, emailAddress)
                    } else {
                        return@map emailAddress
                    }
                }
                .collect(Collectors.toList())
        }
    }
}
