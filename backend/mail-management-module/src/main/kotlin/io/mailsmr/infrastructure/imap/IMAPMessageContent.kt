package io.mailsmr.infrastructure.imap

import com.sun.mail.imap.IMAPMessage
import jakarta.mail.Multipart
import jakarta.mail.Part
import jakarta.mail.internet.MimeMultipart
import mu.KotlinLogging

/**
 * Based on https://javaee.github.io/javamail/FAQ#mainbody
 */
internal class IMAPMessageContent(private val nativeMessage: IMAPMessage, private val peek: Boolean = false) {
    private val logger = KotlinLogging.logger {}

    private var isTextHtml = false

    fun content() = getTextOfMessage()

    private fun getTextOfMessage(): String {
        nativeMessage.peek = peek

        return if (nativeMessage.isMimeType(MIME_TYPE_TEXT_PLAIN)) {
            nativeMessage.content.toString()
        } else {
            val mimeMultipart: MimeMultipart = nativeMessage.content as MimeMultipart
            if (peek) {
                getText(mimeMultipart.getBodyPart(0))
            } else {
                getText(mimeMultipart.parent)
            }
        }
    }

    private fun getText(part: Part): String {
        return when {
            part.isMimeType(MIME_TYPE_TEXT) -> {
                handleMimeTypeText(part)
            }
            part.isMimeType(MIME_TYPE_MULTIPART_ALTERNATIVE) -> {
                handleMimeTypeMultipartAlternative(part)
            }
            part.isMimeType(MIME_TYPE_MULTIPART) -> {
                handleMimeTypeMultipartExceptAlternative(part)
            }
            else -> {
                logger.warn { "${part.contentType} is not supported." }
                ""
            }
        }
    }

    private fun handleMimeTypeText(part: Part): String {
        val messageContent = part.getContent() as String
        isTextHtml = part.isMimeType(MIME_TYPE_TEXT_HTML)
        return messageContent
    }

    private fun handleMimeTypeMultipartAlternative(part: Part): String {
        val multipart: Multipart = part.getContent() as Multipart
        var text: String? = null
        for (i in 0 until multipart.getCount()) {
            val bodyPart: Part = multipart.getBodyPart(i)
            if (bodyPart.isMimeType(MIME_TYPE_TEXT_PLAIN)) {
                if (text == null) {
                    text = getText(bodyPart)
                }
            } else if (bodyPart.isMimeType(MIME_TYPE_TEXT_HTML)) {
                // prefer html text over plain text
                return getText(bodyPart)
            } else {
                return getText(bodyPart)
            }
        }
        return text ?: ""
    }

    private fun handleMimeTypeMultipartExceptAlternative(part: Part): String {
        val mp: Multipart = part.content as Multipart
        for (i in 0 until mp.count) {
            return getText(mp.getBodyPart(i))
        }
        return ""
    }

    companion object {
        const val MIME_TYPE_TEXT = "text/*"
        const val MIME_TYPE_TEXT_PLAIN = "text/plain"
        const val MIME_TYPE_TEXT_HTML = "text/html"
        const val MIME_TYPE_MULTIPART = "multipart/*"
        const val MIME_TYPE_MULTIPART_ALTERNATIVE = "multipart/alternative"
    }
}
