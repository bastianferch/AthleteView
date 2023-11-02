package ase.athlete_view.common.exception.entity

import lombok.NoArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@NoArgsConstructor
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
class ValidationException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
