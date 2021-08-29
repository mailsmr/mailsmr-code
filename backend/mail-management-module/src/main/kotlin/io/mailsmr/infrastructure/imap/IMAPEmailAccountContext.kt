package io.mailsmr.infrastructure.imap

import com.sun.mail.imap.IMAPStore

internal data class IMAPEmailAccountContext(
    val emailAddress: String,
    val store: IMAPStore,
    val idleManager: IMAPIdleManager
)
