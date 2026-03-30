package app.web.bekh20d.habit_tracker.service

import app.web.bekh20d.habit_tracker.exception.InvalidCredentialsException
import app.web.bekh20d.habit_tracker.exception.UnverifiedUserException
import app.web.bekh20d.habit_tracker.model.User
import app.web.bekh20d.habit_tracker.repository.UserRepository
import app.web.bekh20d.habit_tracker.util.JwtUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
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

        // Save and return user
        return userRepository.save(user)
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
}
