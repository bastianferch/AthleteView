/*-
 * #%L
 * athlete_view
 * %%
 * Copyright (C) 2023 - 2024 TU Wien INSO ASE GROUP 5 WS2023
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package ase.athlete_view.common.exception

import ase.athlete_view.common.exception.entity.*
import ase.athlete_view.common.exception.fitimport.DuplicateFitFileException
import ase.athlete_view.config.rate_limit.FailedLoginInterceptor
import ase.athlete_view.common.exception.fitimport.InvalidFitFileException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.format.DateTimeParseException

/**
 * Contains handlers for common [RuntimeException]s which might be thrown across all domains and layers
 * and require no further handling or wrapping.
 */
@ControllerAdvice
class GlobalExceptionHandler {
    private val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var failedLoginInterceptor: FailedLoginInterceptor

    @ExceptionHandler(ValidationException::class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    fun handleValidationException(ex: ValidationException): ExceptionResponseDTO {
        log.warn { "Validation exception: ${ex.message}" }
        return ExceptionResponseDTO(HttpStatus.UNPROCESSABLE_ENTITY, ex.message)
    }

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    fun handleNotFound(ex: NotFoundException): ExceptionResponseDTO {
        log.warn { "Not Found exception: ${ex.message}" }
        return ExceptionResponseDTO(HttpStatus.NOT_FOUND, ex.message)
    }

    @ExceptionHandler(BadCredentialsException::class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    fun handleBadCredentials(ex: BadCredentialsException, webRequest: HttpServletRequest): ExceptionResponseDTO {
        log.warn { "Bad Credentials exception: ${ex.message}" }
        failedLoginInterceptor.onFailedLogin(webRequest)
        return ExceptionResponseDTO(HttpStatus.NOT_FOUND, ex.message)
    }

    @ExceptionHandler(ForbiddenException::class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    fun handleForbidden(ex: ForbiddenException): ExceptionResponseDTO {
        log.warn {"Invalid request: ${ex.message}"}
        return ExceptionResponseDTO(HttpStatus.FORBIDDEN, ex.message)
    }

    @ExceptionHandler(InternalException::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun handleForbidden(ex: InternalException): ExceptionResponseDTO {
        log.warn {"InternalException: ${ex.message}"}
        return ExceptionResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, ex.message)
    }

    @ExceptionHandler(ConflictException::class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ResponseBody
    fun handleBadCredentials(ex: ConflictException): ExceptionResponseDTO {
        log.warn { "ConflictException: ${ex.message}" }
        return ExceptionResponseDTO(HttpStatus.CONFLICT, ex.message)
    }

    @ExceptionHandler(DateTimeParseException::class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    fun handleDateTimeParse(ex: DateTimeParseException): ExceptionResponseDTO {
        log.warn { "DateTimeParseException : ${ex.message}" }
        return ExceptionResponseDTO(HttpStatus.UNPROCESSABLE_ENTITY, "Could not parse a date")
    }

    @ExceptionHandler(DuplicateFitFileException::class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ResponseBody
    fun handleDuplicateFitFile(ex: DuplicateFitFileException): ExceptionResponseDTO {
        log.warn { "DuplicateFitFileException : ${ex.message}" }
        return ExceptionResponseDTO(HttpStatus.CONFLICT, "File already in-store!")
    }

    @ExceptionHandler(AlreadyExistsException::class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ResponseBody
    fun handleAlreadyExists(ex: AlreadyExistsException): ExceptionResponseDTO {
        log.warn { "AlreadyExistsException : ${ex.message}" }
        return ExceptionResponseDTO(HttpStatus.CONFLICT, ex.message)
    }

    @ExceptionHandler(RateLimitExceededException::class)
    @ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS)
    @ResponseBody
    fun handleRateLimitExceeded(ex: RateLimitExceededException): ExceptionResponseDTO {
        log.warn { "RateLimitExceededException : ${ex.message}" }
        return ExceptionResponseDTO(HttpStatus.TOO_MANY_REQUESTS, ex.message)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class, MissingServletRequestParameterException::class, MethodArgumentTypeMismatchException::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun handleSpringWebExceptions(ex: Exception): ExceptionResponseDTO {
        log.warn { "Spring Web Exception: ${ex.javaClass}: ${ex.message}" }
        return ExceptionResponseDTO(HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(NoMapDataException::class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    fun handleNoMapDataException(ex: NoMapDataException): ExceptionResponseDTO {
        log.warn { "NoMapDataException: ${ex.message}" }
        return ExceptionResponseDTO(HttpStatus.NOT_FOUND, ex.message)
    }

    @ExceptionHandler(InvalidFitFileException::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun handleInvalidFitFileException(ex: InvalidFitFileException): ExceptionResponseDTO {
        log.warn { "InvalidFitFileException: ${ex.message}" }
        return ExceptionResponseDTO(HttpStatus.BAD_REQUEST, ex.message)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun handleRemainingExceptions(ex: Exception): ExceptionResponseDTO {
        log.warn { "Unexpected Exception : ${ex.javaClass}: ${ex.message}" }
        return ExceptionResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error")
    }

}
