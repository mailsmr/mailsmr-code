package io.mailsmr.infrastructure.imap

import com.sun.mail.imap.IMAPFolder
import jakarta.mail.Message
import mu.KotlinLogging
import java.util.*
import java.util.stream.Collectors

internal class IMAPMessage(
    val nativeMessage: com.sun.mail.imap.IMAPMessage
) {
    private val logger = KotlinLogging.logger {}

    val uid: Long
    val messageId: String

    fun getEmailEnvelope(receiverEmailAccountId: Int): IMAPEmailEnvelope {
        return IMAPEmailEnvelope.fromMessage(this, receiverEmailAccountId)
    }

    val textFromMessage: String
        get() {
            val imapMessageContent = IMAPMessageContent(nativeMessage)
            return imapMessageContent.content
        }

    companion object {
        const val UID_UNAVAILABLE = -1

        fun getFromNativeMessage(message: Message): IMAPMessage {
            return IMAPMessage(message as com.sun.mail.imap.IMAPMessage)
        }

        fun getFromNativeMessages(messagesArray: Array<Message>): List<IMAPMessage> {
            return Arrays.stream(messagesArray)
                .map(Companion::getFromNativeMessage)
                .collect(Collectors.toList())
        }
    }

    init {
        val tempUid: Long = try {
            (nativeMessage.folder as IMAPFolder).getUID(nativeMessage)
        } catch (e: Exception) {
            UID_UNAVAILABLE.toLong()
        }
        uid = tempUid
        val tempMessageId: String = try {
            nativeMessage.messageID
        } catch (e: Exception) {
            "NOT AVAILABLE"
        }
        messageId = tempMessageId
    }
}
