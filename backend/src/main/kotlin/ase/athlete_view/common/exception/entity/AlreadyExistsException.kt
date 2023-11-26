package ase.athlete_view.common.exception.entity

import lombok.NoArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * A [RuntimeException] that should be used when something (primarily an entity)
 * a user wants to create already exists. The exception will automatically be caught by the framework
 * and the request will fail with [HttpStatus.CONFLICT].
 */
@ResponseStatus(HttpStatus.CONFLICT)
class AlreadyExistsException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
