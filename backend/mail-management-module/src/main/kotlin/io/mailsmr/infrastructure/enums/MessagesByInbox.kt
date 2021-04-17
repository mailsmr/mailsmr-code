package io.mailsmr.infrastructure.enums

import java.io.Serializable

internal enum class MessagesByInbox : Serializable {
    MESSAGE_COUNT_BY_NAME,
    NEW_MESSAGE_COUNT_BY_NAME,
    DELETED_MESSAGE_COUNT_BY_NAME,
    UNREAD_MESSAGE_COUNT_BY_NAME,
    MESSAGE_COUNT_BY_FULL_NAME,
    NEW_MESSAGE_COUNT_BY_FULL_NAME,
    DELETED_MESSAGE_COUNT_BY_FULL_NAME,
    UNREAD_MESSAGE_COUNT_BY_FULL_NAME
}
