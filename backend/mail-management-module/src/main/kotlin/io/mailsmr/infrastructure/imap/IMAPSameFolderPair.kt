package io.mailsmr.infrastructure.imap

import com.sun.mail.imap.IMAPFolder

data class IMAPSameFolderPair(
    val directAccessFolder: IMAPFolder,
    val notifierFolder: IMAPFolder,
)
