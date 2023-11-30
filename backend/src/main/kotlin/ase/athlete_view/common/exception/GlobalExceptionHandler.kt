package ase.athlete_view.common.exception

import ase.athlete_view.common.exception.entity.ConflictException
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.format.DateTimeParseException

/**
 * Contains handlers for common [RuntimeException]s which might be thrown across all domains and layers
 * and require no further handling or wrapping.
 */
@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(ValidationException::class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    fun handleValidationException(ex: ValidationException): ExceptionResponseDTO {
        logger.warn { "Validation exception: ${ex.message}" }
        return ExceptionResponseDTO(HttpStatus.UNPROCESSABLE_ENTITY, ex.message)
    }

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    fun handleNotFound(ex: NotFoundException): ExceptionResponseDTO {
        logger.warn { "Not Found exception: ${ex.message}" }
        return ExceptionResponseDTO(HttpStatus.NOT_FOUND, ex.message)
    }

    @ExceptionHandler(BadCredentialsException::class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    fun handleBadCredentials(ex: BadCredentialsException): ExceptionResponseDTO {
        logger.warn { "Bad Credentials exception: ${ex.message}" }
        return ExceptionResponseDTO(HttpStatus.NOT_FOUND, ex.message)
    }

    @ExceptionHandler(ConflictException::class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ResponseBody
    fun handleBadCredentials(ex: ConflictException): ExceptionResponseDTO {
        logger.warn { "ConflictException: ${ex.message}" }
        return ExceptionResponseDTO(HttpStatus.CONFLICT, ex.message)
    }

    @ExceptionHandler(DateTimeParseException::class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    fun handleDateTimeParse(ex: DateTimeParseException): ExceptionResponseDTO {
        logger.warn { "DateTimeParseException : ${ex.message}" }
        return ExceptionResponseDTO(HttpStatus.UNPROCESSABLE_ENTITY, "Could not parse a date")
    }
}
