package io.mailsmr.infrastructure.imap.providers

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.sun.mail.imap.IMAPStore
import io.mailsmr.infrastructure.enums.FolderSpecialUse
import io.mailsmr.infrastructure.imap.IMAPEmailAccount

internal class IMAPEmailAccountGmail (
    store: IMAPStore,
    specialFoldersBiMapping: BiMap<FolderSpecialUse, String> = HashBiMap.create()
) : IMAPEmailAccount(store, specialFoldersBiMapping) {

    init {
        specialFoldersBiMapping.putIfAbsent(FolderSpecialUse.TRASH, "[Gmail]/Trash")
    }
}
