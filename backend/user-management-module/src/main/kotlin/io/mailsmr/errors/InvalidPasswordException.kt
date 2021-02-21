package io.mailsmr.errors

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
class InvalidPasswordException : RuntimeException {
    /**
     * Returning Error with HTTP Status HttpStatus.UNPROCESSABLE_ENTITY - 422
     */
    constructor() : super("Invalid Password")

    /**
     * Returning Error with HTTP Status HttpStatus.UNPROCESSABLE_ENTITY - 422
     */
    constructor(message: String?) : super(message)

    /**
     * Returning Error with HTTP Status HttpStatus.UNPROCESSABLE_ENTITY - 422
     */
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    /**
     * Returning Error with HTTP Status HttpStatus.UNPROCESSABLE_ENTITY - 422
     */
    constructor(cause: Throwable?) : super(cause)

    /**
     * Returning Error with HTTP Status HttpStatus.UNPROCESSABLE_ENTITY - 422
     */
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(message, cause, enableSuppression, writableStackTrace)
}
