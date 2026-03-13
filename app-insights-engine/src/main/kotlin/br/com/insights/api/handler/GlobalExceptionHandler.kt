package br.com.insights.api.handler

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ProblemDetail {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        return ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
            title  = "Validation Failed"
            setProperty("errors", errors)
        }
    }

    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    fun handleBadRequest(ex: RuntimeException): ProblemDetail {
        log.warn("Bad request: {}", ex.message)
        return ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
            title  = "Bad Request"
            detail = ex.message
        }
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ProblemDetail {
        log.error("Unexpected error", ex)
        return ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR).apply {
            title  = "Internal Server Error"
            detail = "Erro inesperado. Tente novamente."
        }
    }
}
