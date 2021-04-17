package io.mailsmr.infrastructure.imap

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.sun.mail.imap.IMAPStore
import io.mailsmr.infrastructure.enums.FolderSpecialUse
import io.mailsmr.infrastructure.exceptions.FolderNotFoundException
import io.mailsmr.infrastructure.imap.providers.IMAPEmailAccountGmail
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

internal open class IMAPEmailAccount(
    private val store: IMAPStore,
    private val specialFoldersBiMap: BiMap<FolderSpecialUse, String>
) : AutoCloseable {
    private val logger = KotlinLogging.logger {}

    private val folderMap: ConcurrentHashMap<String, IMAPFolder> = ConcurrentHashMap()

    fun getFolders(): List<IMAPFolder> {
        return Arrays.stream(store.defaultFolder.list())
            .map { nativeImapFolder ->
                val folderName = nativeImapFolder.fullName
                return@map folderMap.computeIfAbsent(folderName) {
                    return@computeIfAbsent mapAndReturnFolderByNativeFolder(nativeImapFolder as com.sun.mail.imap.IMAPFolder)
                }
            }
            .collect(Collectors.toList())
    }

    fun getFolder(folderName: String): IMAPFolder {
        return folderMap.computeIfAbsent(folderName) {
            return@computeIfAbsent mapAndReturnFolderByName(folderName)
        }
    }

    private fun mapAndReturnFolderByNativeFolder(nativeImapFolder: com.sun.mail.imap.IMAPFolder): IMAPFolder {
        val folderName = nativeImapFolder.fullName
        return IMAPFolder(
            nativeImapFolder,
            this,
            specialFoldersBiMap.inverse()[folderName] ?: FolderSpecialUse.NONE
        )
    }

    private fun mapAndReturnFolderByName(folderName: String): IMAPFolder {
        return IMAPFolder(
            store,
            this,
            folderName,
            specialFoldersBiMap.inverse()[folderName] ?: FolderSpecialUse.NONE
        )
    }

    fun getSpecialFolder(specialUse: FolderSpecialUse): IMAPFolder {
        val folder: String =
            specialFoldersBiMap[specialUse] ?: throw FolderNotFoundException("${specialUse.name}: Folder not found")
        return getFolder(folder)
    }

    override fun close() {
        if (store.isConnected) {
            store.close()
        }
        for (folder in folderMap.values) {
            if (folder.isOpen) {
                folder.close()
            }
        }
    }

    companion object {
        private const val STORE_TYPE = "imaps"

        fun getInstanceForEmailAddress(emailAddress: String, store: IMAPStore): IMAPEmailAccount {
            return getInstanceForEmailAddress(emailAddress, store, HashBiMap.create())
        }

        fun getInstanceForEmailAddress(
            emailAddress: String,
            store: IMAPStore,
            specialFoldersBiMapping: BiMap<FolderSpecialUse, String>
        ): IMAPEmailAccount {
            return when (extractDomainFromEmailAddress(emailAddress)) {
                "gmail.com" -> IMAPEmailAccountGmail(
                    store,
                    specialFoldersBiMapping
                )
                else -> IMAPEmailAccount(store, specialFoldersBiMapping)
            }
        }

        private fun extractDomainFromEmailAddress(emailAddress: String): String {
            return emailAddress.split("@").toTypedArray()[1]
        }
    }
}
