package app.web.bekh20d.habit_tracker.exception

import app.web.bekh20d.habit_tracker.dto.MessageResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = ex.bindingResult.fieldErrors.map { it.defaultMessage ?: "Validation error" }
        return ResponseEntity.badRequest().body(
            mapOf(
                "message" to "Validation failed",
                "errors" to errors
            )
        )
    }

    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmail(ex: DuplicateEmailException): ResponseEntity<MessageResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(MessageResponse(ex.message ?: "Email already registered"))
    }
    
    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<MessageResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(MessageResponse(ex.message ?: "Invalid credentials"))
    }
    
    @ExceptionHandler(UnverifiedUserException::class)
    fun handleUnverifiedUser(ex: UnverifiedUserException): ResponseEntity<MessageResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(MessageResponse(ex.message ?: "Email not verified"))
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidToken(ex: InvalidTokenException): ResponseEntity<MessageResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(MessageResponse(ex.message ?: "Invalid or expired token"))
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<MessageResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(MessageResponse(ex.message ?: "Resource not found"))
    }
}
