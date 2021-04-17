package io.mailsmr.infrastructure.enums

import jakarta.mail.Folder

internal enum class FolderOpenMode(val mode: Int) {
    READ_ONLY(Folder.READ_ONLY),
    READ_WRITE(Folder.READ_WRITE);
}
