package ase.athlete_view.common.exception

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Contains handlers for common [RuntimeException]s which might be thrown across all domains and layers
 * and require no further handling or wrapping.
 */
@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ValidationException::class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    fun handleValidationException(ex: ValidationException): ExceptionResponseDTO {
        // ToDo: log
        return ExceptionResponseDTO(HttpStatus.UNPROCESSABLE_ENTITY, ex.message)
    }

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    fun handleNotFound(ex: NotFoundException): ExceptionResponseDTO {
        // ToDo: log
        return ExceptionResponseDTO(HttpStatus.NOT_FOUND, ex.message)
    }
}
