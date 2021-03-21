package io.mailsmr.domain.errors

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Returning Error with HTTP Status HttpStatus.UNAUTHORIZED - 401
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
class ExpiredOrRevokedTokenException : RuntimeException {

    constructor() : super("The provided token is expired or revoked.")

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(cause: Throwable?) : super(cause)

    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(
        message,
        cause,
        enableSuppression,
        writableStackTrace
    )
}
