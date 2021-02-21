package io.mailsmr.domain.errors

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

/**
 * Returning Error with HTTP Status HttpStatus.UNPROCESSABLE_ENTITY - 422
 */
@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
class InvalidCredentialsException : RuntimeException {

    constructor()

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(cause: Throwable?) : super(cause)

    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(message, cause, enableSuppression, writableStackTrace)
}
