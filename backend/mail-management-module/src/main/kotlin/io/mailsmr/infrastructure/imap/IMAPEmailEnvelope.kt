package io.mailsmr.infrastructure.imap

import jakarta.mail.Address
import jakarta.mail.Flags
import jakarta.mail.Message.RecipientType.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage.RecipientType.NEWSGROUPS
import java.io.Serializable
import java.util.*
import java.util.stream.Collectors
import jakarta.mail.Message.RecipientType.TO

internal class IMAPEmailEnvelope(
    private var uid: Long? = null,
    private var messageId: String? = null,
    private var from: List<String>? = null,
    private var to: List<String>? = null,
    private var cc: List<String>? = null,
    private var bcc: List<String>? = null,
    private var newsgroup: List<String>? = null,
    private var replyTo: List<String>? = null,
    private var sentDate: Date? = null,
    private var receivedDate: Date? = null,
    private var subject: String? = null,
    private var folder: String? = null,
    private var expunged: Boolean? = null,
    private var answered: Boolean? = null,
    private var deleted: Boolean? = null,
    private var draft: Boolean? = null,
    private var flagged: Boolean? = null,
    private var recent: Boolean? = null,
    private var seen: Boolean? = null,
    private var receiverEmailAccountId: Int? = null
) : Serializable, Comparable<IMAPEmailEnvelope> {

    constructor(IMAPEmail: IMAPEmailEnvelope) : this(
        uid = IMAPEmail.uid,
        messageId = IMAPEmail.messageId,
        from = IMAPEmail.from,
        to = IMAPEmail.to,
        cc = IMAPEmail.cc,
        bcc = IMAPEmail.bcc,
        newsgroup = IMAPEmail.newsgroup,
        replyTo = IMAPEmail.replyTo,
        sentDate = IMAPEmail.sentDate,
        receivedDate = IMAPEmail.receivedDate,
        subject = IMAPEmail.subject,
        expunged = IMAPEmail.expunged,
        folder = IMAPEmail.folder,
        answered = IMAPEmail.answered,
        deleted = IMAPEmail.deleted,
        draft = IMAPEmail.draft,
        flagged = IMAPEmail.flagged,
        recent = IMAPEmail.recent,
        seen = IMAPEmail.seen,
        receiverEmailAccountId = IMAPEmail.receiverEmailAccountId
    )

    override fun compareTo(e: IMAPEmailEnvelope): Int {
        return this.receivedDate?.compareTo(e.receivedDate) ?: 0
    }

    companion object {
        fun fromMessage(message: IMAPMessage, receiverEmailAccountId: Int): IMAPEmailEnvelope {
            return if (message.nativeMessage.isExpunged) {
                fromDeletedMessage(message, receiverEmailAccountId)
            } else {
                fromExistingMessage(message, receiverEmailAccountId)
            }
        }

        fun fromMessages(messages: List<IMAPMessage>, receiverEmailAccountId: Int): List<IMAPEmailEnvelope> {
            return messages.stream().parallel()
                .map { message: IMAPMessage -> fromMessage(message, receiverEmailAccountId) }
                .collect(Collectors.toList())
        }

        private fun fromDeletedMessage(message: IMAPMessage, receiverEmailAccountId: Int): IMAPEmailEnvelope {
            val nativeMessage = message.nativeMessage
            val emailEnvelope = IMAPEmailEnvelope()
            emailEnvelope.uid = message.uid
            emailEnvelope.messageId = message.messageId
            emailEnvelope.expunged = nativeMessage.isExpunged
            emailEnvelope.folder = nativeMessage.folder.fullName
            emailEnvelope.receiverEmailAccountId = receiverEmailAccountId
            return emailEnvelope
        }

        private fun fromExistingMessage(message: IMAPMessage, receiverEmailAccountId: Int): IMAPEmailEnvelope {
            val nativeMessage = message.nativeMessage
            return IMAPEmailEnvelope(
                message.uid,
                message.messageId,
                convertAddressArrayToStringList(nativeMessage.from),
                convertAddressArrayToStringList(nativeMessage.getRecipients(TO)),
                convertAddressArrayToStringList(nativeMessage.getRecipients(CC)),
                convertAddressArrayToStringList(nativeMessage.getRecipients(BCC)),
                convertAddressArrayToStringList(nativeMessage.getRecipients(NEWSGROUPS)),
                convertAddressArrayToStringList(nativeMessage.replyTo),
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
                receiverEmailAccountId
            )
        }

        private fun convertAddressArrayToStringList(addresses: Array<Address>): List<String> {
            return Arrays.stream(addresses)
                .parallel()
                .map { address ->
                    val internetAddress: InternetAddress = address as InternetAddress
                    val emailAddress: String = internetAddress.getAddress()
                    val personal: String = internetAddress.getPersonal()
                    return@map String.format("%s <%s>", personal, emailAddress)
                }
                .collect(Collectors.toList())
        }
    }

}
