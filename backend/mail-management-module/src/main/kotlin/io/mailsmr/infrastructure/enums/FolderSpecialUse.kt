package io.mailsmr.infrastructure.enums

/**
 * Based on Geary
 * https://gitlab.gnome.org/GNOME/geary/-/blob/mainline/src/engine/api/geary-folder.vala
 */
internal enum class FolderSpecialUse {
    /**
     * No special type, likely user-created.
     */
    NONE,  // Well-known concrete folders

    /**
     * Denotes the inbox for the account.
     */
    INBOX,

    /**
     * Stores email to be kept.
     */
    ARCHIVE,

    /**
     * Stores email that has not yet been sent.
     */
    DRAFTS,

    /**
     * Stores spam, malware and other kinds of unwanted email.
     */
    JUNK,

    /**
     * Stores email that is waiting to be sent.
     */
    OUTBOX,

    /**
     * Stores email that has been sent.
     */
    SENT,

    /**
     * Stores email that is to be deleted.
     */
    TRASH,  // Virtual folders

    /**
     * A view of all email in an account.
     */
    ALL_MAIL,

    /**
     * A view of all flagged/starred email in an account.
     */
    FLAGGED,

    /**
     * A view of email the server thinks is important.
     */
    IMPORTANT,

    /**
     * A view of email matching some kind of search criteria.
     */
    SEARCH,

    /**
     * A folder with an application-defined use.
     */
    CUSTOM;

    val isOutgoing: Boolean
        get() = this == SENT || this == OUTBOX
}
