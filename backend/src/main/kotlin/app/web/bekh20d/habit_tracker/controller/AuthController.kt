package app.web.bekh20d.habit_tracker.controller

import app.web.bekh20d.habit_tracker.dto.LoginRequest
import app.web.bekh20d.habit_tracker.dto.LoginResponse
import app.web.bekh20d.habit_tracker.dto.MessageResponse
import app.web.bekh20d.habit_tracker.dto.SignupRequest
import app.web.bekh20d.habit_tracker.exception.DuplicateEmailException
import app.web.bekh20d.habit_tracker.service.AuthService
import jakarta.validation.Valid
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<MessageResponse> {
        try {
            authService.signup(request.email, request.password)
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessageResponse("User registered successfully. Please check your email to verify your account."))
        } catch (e: DataIntegrityViolationException) {
            // Handle duplicate email constraint violation
            if (e.message?.contains("email") == true || e.message?.contains("Duplicate") == true) {
                throw DuplicateEmailException("Email already registered")
            }
            throw e
        }
    }
    
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val token = authService.login(request.email, request.password)
        return ResponseEntity.ok(LoginResponse(token))
    }
}
