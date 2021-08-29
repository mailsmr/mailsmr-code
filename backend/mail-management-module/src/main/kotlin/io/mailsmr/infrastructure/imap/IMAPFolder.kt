package io.mailsmr.infrastructure.imap

import com.sun.mail.imap.IMAPStore
import io.mailsmr.infrastructure.enums.FolderOpenMode
import io.mailsmr.infrastructure.enums.FolderSpecialUse
import io.mailsmr.infrastructure.enums.MessageCountType
import io.mailsmr.infrastructure.exceptions.FolderNotFoundException
import io.mailsmr.infrastructure.exceptions.MessagingRuntimeException
import io.mailsmr.infrastructure.imap.interfaces.MailAccountFolderMessageListener
import jakarta.mail.FetchProfile
import jakarta.mail.Flags
import jakarta.mail.Message
import jakarta.mail.MessagingException
import mu.KotlinLogging
import com.sun.mail.imap.IMAPMessage as NativeIMAPMessage

internal class IMAPFolder(
    private val nativeImapFolders: IMAPSameFolderPair,
    private val account: IMAPEmailAccount,
    private val folderSpecialUse: FolderSpecialUse,
    val accountContext: IMAPEmailAccountContext,
) : AutoCloseable {
    private val logger = KotlinLogging.logger {}

    private val nativeImapFolder = nativeImapFolders.directAccessFolder

    private var imapEmailFolderNotifier: IMAPEmailFolderNotifier? = null

    constructor(
        store: IMAPStore,
        account: IMAPEmailAccount,
        folderName: String?,
        folderSpecialUse: FolderSpecialUse,
        accountContext: IMAPEmailAccountContext
    ) : this(
        IMAPSameFolderPair(
            store.getFolder(folderName) as com.sun.mail.imap.IMAPFolder,
            store.getFolder(folderName) as com.sun.mail.imap.IMAPFolder
        ),
        account,
        folderSpecialUse,
        accountContext
    )

    fun getMessage(messageNumber: Long): IMAPMessage {
        return getMessage(
            messageNumber,
            FolderOpenMode.READ_WRITE
        ) // Open in READ_WRITE mode to auto update "read-flag"
    }

    fun getMessage(messageNumber: Long, mode: FolderOpenMode): IMAPMessage {
        return try {
            val message = getNativeImapFolder(mode).getMessageByUID(messageNumber) as NativeIMAPMessage
            IMAPMessage.getFromNativeMessage(message)
        } catch (e: MessagingException) {
            throw MessagingRuntimeException(e)
        }
    }

    val messages: List<IMAPMessage>
        get() {
            val folder = getNativeImapFolder(FolderOpenMode.READ_ONLY)
            val messages: Array<Message> = folder.messages
            val fp = FetchProfile()
            fp.add(FetchProfile.Item.ENVELOPE)
            fp.add(FetchProfile.Item.FLAGS)
            folder.fetch(messages, fp)
            return IMAPMessage.getFromNativeMessages(messages)
        }

    fun copyMessage(message: IMAPMessage, destination: IMAPFolder) {
        destination.getNativeImapFolder(FolderOpenMode.READ_WRITE)
            .use { folder -> nativeImapFolder.copyMessages(arrayOf(message.nativeMessage), folder) }
    }

    fun copyMessage(messageNumber: Int, destination: IMAPFolder) {
        val message = getMessage(messageNumber.toLong())
        this.copyMessage(message, destination)
    }

    fun copyMessage(messageNumber: Int, folderSpecialUse: FolderSpecialUse) {
        val message = getMessage(messageNumber.toLong())
        this.copyMessage(message, folderSpecialUse)
    }

    fun copyMessage(message: IMAPMessage, folderSpecialUse: FolderSpecialUse) {
        val destinationFolder = account.getSpecialFolder(folderSpecialUse)
        this.copyMessage(message, destinationFolder)
    }

    fun getMessageCountByType(type: MessageCountType): Int {
        val folder = getNativeImapFolder(FolderOpenMode.READ_ONLY)
        return when (type) {
            MessageCountType.ALL_MESSAGES -> folder.messageCount
            MessageCountType.NEW_MESSAGES -> folder.newMessageCount
            MessageCountType.UNREAD_MESSAGES -> folder.unreadMessageCount
            MessageCountType.DELETED_MESSAGES -> folder.deletedMessageCount
            else -> throw UnsupportedOperationException(
                java.lang.String.format(
                    "Message count of type \"%s\" not supported",
                    type.name
                )
            )
        }
    }

    fun deleteMail(messageNumber: Long): Boolean {
        try {
            val message = getMessage(messageNumber, FolderOpenMode.READ_WRITE)
            if (folderSpecialUse !== FolderSpecialUse.TRASH) { // delete email completely from trash folder
                copyMessage(message, FolderSpecialUse.TRASH)
            }
            message.nativeMessage.setFlag(Flags.Flag.DELETED, true) // mark as deleted
            getNativeImapFolder(FolderOpenMode.READ_WRITE).expunge() // delete all messages marked as deleted
            return true
        } catch (e: MessagingException) {
            logger.error(e) { e.message }
        } catch (e: FolderNotFoundException) {
            logger.error(e) { e.message }
        }
        return false
    }

    fun subscribe(listener: MailAccountFolderMessageListener) {
        if (imapEmailFolderNotifier == null) {
            imapEmailFolderNotifier = IMAPEmailFolderNotifier(nativeImapFolders.notifierFolder, accountContext)
        }
        imapEmailFolderNotifier!!.addMailAccountFolderNewMessageListener(listener)
    }


    fun disconnect(listener: MailAccountFolderMessageListener) {
        if (imapEmailFolderNotifier != null) {
            imapEmailFolderNotifier!!.removeMailAccountFolderNewMessageListener(listener)
        }
    }


    val isOpen: Boolean
        get() = nativeImapFolder.isOpen

    override fun close() {
        try {
            if (nativeImapFolder.isOpen) {
                nativeImapFolder.close()
            }
        } catch (e: MessagingException) {
            throw MessagingRuntimeException(e)
        }
    }

    internal fun getNativeImapFolder(mode: FolderOpenMode): com.sun.mail.imap.IMAPFolder {
        return try {
            if (nativeImapFolder.isOpen) {
                if (nativeImapFolder.mode != mode.mode) {
                    nativeImapFolder.close(true)
                    nativeImapFolder.open(mode.mode)
                }
            } else {
                nativeImapFolder.open(mode.mode)
            }
            nativeImapFolder
        } catch (e: MessagingException) {
            throw MessagingRuntimeException(e)
        }
    }
}
