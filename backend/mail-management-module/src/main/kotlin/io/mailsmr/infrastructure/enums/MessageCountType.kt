package io.mailsmr.infrastructure.enums

import java.io.Serializable

internal enum class MessageCountType : Serializable {
    ALL_MESSAGES,
    NEW_MESSAGES,
    DELETED_MESSAGES,
    UNREAD_MESSAGES
}
