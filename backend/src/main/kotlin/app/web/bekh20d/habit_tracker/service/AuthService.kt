package app.web.bekh20d.habit_tracker.service

import app.web.bekh20d.habit_tracker.exception.InvalidCredentialsException
import app.web.bekh20d.habit_tracker.exception.InvalidTokenException
import app.web.bekh20d.habit_tracker.exception.UnverifiedUserException
import app.web.bekh20d.habit_tracker.model.EmailVerificationToken
import app.web.bekh20d.habit_tracker.model.User
import app.web.bekh20d.habit_tracker.repository.EmailVerificationTokenRepository
import app.web.bekh20d.habit_tracker.repository.UserRepository
import app.web.bekh20d.habit_tracker.util.JwtUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val emailService: EmailService
) {

    fun signup(email: String, password: String): User {
        // Hash password using BCrypt
        val hashedPassword = passwordEncoder.encode(password) ?: throw IllegalStateException("Password encoding failed")

        // Create user with verified=false
        val user = User(
            email = email,
            password = hashedPassword,
            verified = false
        )

        // Save user
        val savedUser = userRepository.save(user)
        
        // Generate verification token with 24-hour expiry
        val token = UUID.randomUUID().toString()
        val expiryDate = LocalDateTime.now().plusHours(24)
        val verificationToken = EmailVerificationToken(
            userId = savedUser.id,
            token = token,
            expiryDate = expiryDate
        )
        emailVerificationTokenRepository.save(verificationToken)
        
        // Send verification email
        emailService.sendVerificationEmail(email, token)
        
        return savedUser
    }
    
    fun login(email: String, password: String): String {
        // Find user by email
        val user = userRepository.findByEmail(email)
            ?: throw InvalidCredentialsException("Invalid email or password")
        
        // Check if user is verified
        if (!user.verified) {
            throw UnverifiedUserException("Email not verified. Please check your email.")
        }
        
        // Validate password
        if (!passwordEncoder.matches(password, user.password)) {
            throw InvalidCredentialsException("Invalid email or password")
        }
        
        // Generate and return JWT token
        return jwtUtil.generateToken(user.id, user.email)
    }
    
    fun verifyEmail(token: String) {
        // Find token in database
        val verificationToken = emailVerificationTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Invalid or expired token")
        
        // Check if token is expired
        if (LocalDateTime.now().isAfter(verificationToken.expiryDate)) {
            emailVerificationTokenRepository.delete(verificationToken)
            throw InvalidTokenException("Token has expired")
        }
        
        // Update user verified status
        val user = userRepository.findById(verificationToken.userId).orElseThrow {
            throw InvalidTokenException("User not found")
        }
        
        // Create updated user with verified=true
        val updatedUser = User(
            id = user.id,
            email = user.email,
            password = user.password,
            verified = true,
            createdAt = user.createdAt
        )
        userRepository.save(updatedUser)
        
        // Delete verification token after successful verification
        emailVerificationTokenRepository.delete(verificationToken)
    }
}
