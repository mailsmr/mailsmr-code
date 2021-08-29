package io.mailsmr.infrastructure.imap.providers

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import io.mailsmr.infrastructure.enums.FolderSpecialUse
import io.mailsmr.infrastructure.imap.IMAPEmailAccount
import io.mailsmr.infrastructure.imap.IMAPEmailAccountContext

internal class IMAPEmailAccountGmail(
    accountContext: IMAPEmailAccountContext,
    specialFoldersBiMapping: BiMap<FolderSpecialUse, String> = HashBiMap.create()
) : IMAPEmailAccount(accountContext, specialFoldersBiMapping) {

    init {
        specialFoldersBiMapping.putIfAbsent(FolderSpecialUse.TRASH, "[Gmail]/Trash")
    }
}
