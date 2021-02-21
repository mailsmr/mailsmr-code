package io.mailsmr.domain.errors

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Returning Error with HTTP Status HttpStatus.UNAUTHORIZED - 401
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
class UnauthorizedTokenCombinationException : RuntimeException {

    constructor() : super("Provided tokens do not belong to the same user.")

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
