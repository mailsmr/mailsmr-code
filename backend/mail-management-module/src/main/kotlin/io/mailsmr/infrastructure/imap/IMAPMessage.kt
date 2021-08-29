package io.mailsmr.infrastructure.imap

import com.sun.mail.imap.IMAPFolder
import jakarta.mail.Message
import java.util.*
import java.util.stream.Collectors

internal class IMAPMessage(
    val nativeMessage: com.sun.mail.imap.IMAPMessage
) {

    val uid: Long = try {
        (nativeMessage.folder as IMAPFolder).getUID(nativeMessage)
    } catch (e: Exception) {
        UID_UNAVAILABLE.toLong()
    }
    val messageId: String = try {
        nativeMessage.messageID
    } catch (e: Exception) {
        "NOT AVAILABLE"
    }

    fun getEmailEnvelope(accountEmailAddress: String): IMAPEmailEnvelope {
        return IMAPEmailEnvelope.fromMessage(this, accountEmailAddress)
    }

    val content: String
        get() {
            return IMAPMessageContent(nativeMessage).content()
        }

    val contentPeek: String
        get() {
            return IMAPMessageContent(nativeMessage, true).content()
        }

    companion object {
        const val UID_UNAVAILABLE = -1

        fun getFromNativeMessage(message: Message): IMAPMessage {
            return IMAPMessage(message as com.sun.mail.imap.IMAPMessage)
        }

        fun getFromNativeMessages(messagesArray: Array<Message>): List<IMAPMessage> {
            return Arrays.stream(messagesArray)
                .parallel()
                .map(Companion::getFromNativeMessage)
                .collect(Collectors.toList())
        }
    }
}
