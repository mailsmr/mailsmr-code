package io.mailsmr.infrastructure.imap

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import io.mailsmr.infrastructure.enums.FolderSpecialUse
import io.mailsmr.infrastructure.exceptions.FolderNotFoundException
import io.mailsmr.infrastructure.imap.providers.IMAPEmailAccountGmail
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

internal open class IMAPEmailAccount(
    private val accountContext: IMAPEmailAccountContext,
    private val specialFoldersBiMap: BiMap<FolderSpecialUse, String>,
) : AutoCloseable {
    private val logger = KotlinLogging.logger {}

    private val store = accountContext.store
    private val folderMap: ConcurrentHashMap<String, IMAPFolder> = ConcurrentHashMap()


    fun getFolders(): List<IMAPFolder> {
        return Arrays.stream(store.defaultFolder.list())
            .map { nativeImapFolder ->
                val folderName = nativeImapFolder.fullName
                return@map folderMap.computeIfAbsent(folderName) {
                    return@computeIfAbsent mapAndReturnFolderByNativeFolder(
                        IMAPSameFolderPair(
                            nativeImapFolder as com.sun.mail.imap.IMAPFolder,
                            store.getFolder(folderName) as com.sun.mail.imap.IMAPFolder
                        )
                    )
                }
            }
            .collect(Collectors.toList())
    }

    fun getFolder(folderName: String): IMAPFolder {
        return folderMap.computeIfAbsent(folderName) {
            return@computeIfAbsent mapAndReturnFolderByName(folderName)
        }
    }

    private fun mapAndReturnFolderByNativeFolder(nativeImapFolders: IMAPSameFolderPair): IMAPFolder {
        val folderName = nativeImapFolders.directAccessFolder.fullName
        return IMAPFolder(
            nativeImapFolders,
            this,
            specialFoldersBiMap.inverse()[folderName] ?: FolderSpecialUse.NONE,
            accountContext
        )
    }

    private fun mapAndReturnFolderByName(folderName: String): IMAPFolder {
        return IMAPFolder(
            store,
            this,
            folderName,
            specialFoldersBiMap.inverse()[folderName] ?: FolderSpecialUse.NONE,
            accountContext
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
        fun getInstanceForEmailAddress(
            accountContext: IMAPEmailAccountContext
        ): IMAPEmailAccount {
            return getInstanceForEmailAddress(accountContext, HashBiMap.create())
        }

        fun getInstanceForEmailAddress(
            accountContext: IMAPEmailAccountContext,
            specialFoldersBiMapping: BiMap<FolderSpecialUse, String>,
        ): IMAPEmailAccount {
            return when (extractDomainFromEmailAddress(accountContext.emailAddress)) {
                "gmail.com" -> IMAPEmailAccountGmail(
                    accountContext,
                    specialFoldersBiMapping
                )
                else -> IMAPEmailAccount(accountContext, specialFoldersBiMapping)
            }
        }

        private fun extractDomainFromEmailAddress(emailAddress: String): String {
            return emailAddress.split("@").toTypedArray()[1]
        }
    }
}
