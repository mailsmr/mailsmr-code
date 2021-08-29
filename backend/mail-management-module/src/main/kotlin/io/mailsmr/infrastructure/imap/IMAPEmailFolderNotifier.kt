package io.mailsmr.infrastructure.imap

import io.mailsmr.infrastructure.enums.FolderOpenMode
import io.mailsmr.infrastructure.exceptions.MessagingRuntimeException
import io.mailsmr.infrastructure.imap.interfaces.MailAccountFolderMessageListener
import jakarta.mail.Message
import jakarta.mail.event.*
import mu.KotlinLogging
import java.util.*
import java.util.stream.Collectors
import com.sun.mail.imap.IMAPFolder as NativeIMAPFolder

internal class IMAPEmailFolderNotifier(
    private val nativeImapFolder: NativeIMAPFolder,
    accountContext: IMAPEmailAccountContext
) : MessageCountListener, ConnectionListener, MessageChangedListener {

    private val logger = KotlinLogging.logger {}

    private val idleManager = accountContext.idleManager

    private val listeners: MutableSet<MailAccountFolderMessageListener> = HashSet()
    private val receiverAccountEmailAddress = accountContext.emailAddress

    init {
        nativeImapFolder.open(FolderOpenMode.READ_ONLY.mode)
        nativeImapFolder.addConnectionListener(this)
        nativeImapFolder.addMessageCountListener(this)
        nativeImapFolder.addMessageChangedListener(this)

        startNextWatchCycle()
    }

    private fun startNextWatchCycle() {
        if (listeners.isNotEmpty()) {
            idleManager.watch(nativeImapFolder)
        }
    }

    public fun close() {
        TODO()
    }

    override fun messagesAdded(messageCountEvent: MessageCountEvent) {
        startNextWatchCycle()
        val messages: List<IMAPEmailEnvelope> = messagesArrayToEmailEnvelopeList(messageCountEvent.messages)
        for (listener in listeners) {
            listener.messagesAdded(messages)
        }
    }

    override fun messagesRemoved(messageCountEvent: MessageCountEvent) {
        startNextWatchCycle()
        val messages: List<IMAPEmailEnvelope> = messagesArrayToEmailEnvelopeList(messageCountEvent.messages)
        for (listener in listeners) {
            listener.messagesRemoved(messages)
        }
    }

    override fun messageChanged(messageChangedEvent: MessageChangedEvent) {
        startNextWatchCycle()
        val message = IMAPMessage.getFromNativeMessage(messageChangedEvent.message)
        val envelope = message.getEmailEnvelope(receiverAccountEmailAddress)

        for (listener in listeners) {
            listener.messageChanged(envelope)
        }
    }

    private fun messagesArrayToEmailEnvelopeList(messagesArray: Array<Message>): List<IMAPEmailEnvelope> {
        return IMAPMessage.getFromNativeMessages(messagesArray)
            .stream().map { imapMessage ->
                try {
                    return@map imapMessage.getEmailEnvelope(receiverAccountEmailAddress)
                } catch (e: MessagingRuntimeException) {
                    logger.error(e) { e.message }
                    return@map IMAPEmailEnvelope.NULL_INSTANCE
                }
            }
            .filter { obj: Any? -> Objects.nonNull(obj) }
            .collect(Collectors.toList())
    }


    fun addMailAccountFolderNewMessageListener(listener: MailAccountFolderMessageListener) {
        this.listeners.add(listener)
        startNextWatchCycle()
    }

    fun removeMailAccountFolderNewMessageListener(listener: MailAccountFolderMessageListener) {
        this.listeners.remove(listener)
    }

    override fun opened(e: ConnectionEvent) {
        logger.debug { "Connection opened: $e" }
    }

    override fun disconnected(e: ConnectionEvent) {
        logger.debug { "Disconnected: $e" }
    }

    override fun closed(e: ConnectionEvent) {
        logger.debug { "Connection closed: $e" }
    }
}

