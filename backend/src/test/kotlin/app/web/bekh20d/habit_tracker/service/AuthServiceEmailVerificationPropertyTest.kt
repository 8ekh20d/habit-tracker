package app.web.bekh20d.habit_tracker.service

import app.web.bekh20d.habit_tracker.exception.UnverifiedUserException
import app.web.bekh20d.habit_tracker.model.User
import app.web.bekh20d.habit_tracker.repository.EmailVerificationTokenRepository
import app.web.bekh20d.habit_tracker.repository.UserRepository
import app.web.bekh20d.habit_tracker.util.JwtUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Property-Based Tests for Email Verification Requirement
 * 
 * **Validates: Requirements 3.3**
 * 
 * Property 2: Email Verification Required
 * - Unverified users cannot login (should throw UnverifiedUserException)
 * - Verified users can login successfully with correct credentials
 */
class AuthServiceEmailVerificationPropertyTest : StringSpec({

    "Property 2: unverified users cannot login" {
        val userRepository = mock(UserRepository::class.java)
        val tokenRepository = mock(EmailVerificationTokenRepository::class.java)
        val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()
        val jwtUtil = mock(JwtUtil::class.java)
        val emailService = mock(EmailService::class.java)
        val authService = AuthService(userRepository, tokenRepository, passwordEncoder, jwtUtil, emailService)

        checkAll(5, Arb.string(8..100)) { rawPassword ->
            val email = "test@example.com"
            val hashedPassword = passwordEncoder.encode(rawPassword)
            
            // Create unverified user (verified = false)
            val unverifiedUser = User(
                id = 1L,
                email = email,
                password = hashedPassword,
                verified = false
            )
            
            `when`(userRepository.findByEmail(email)).thenReturn(unverifiedUser)
            
            // Attempt to login should throw UnverifiedUserException
            shouldThrow<UnverifiedUserException> {
                authService.login(email, rawPassword)
            }
        }
    }

    "Property 2: verified users can login with correct credentials" {
        val userRepository = mock(UserRepository::class.java)
        val tokenRepository = mock(EmailVerificationTokenRepository::class.java)
        val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()
        val jwtUtil = mock(JwtUtil::class.java)
        val emailService = mock(EmailService::class.java)
        val authService = AuthService(userRepository, tokenRepository, passwordEncoder, jwtUtil, emailService)

        checkAll(5, Arb.string(8..100)) { rawPassword ->
            val email = "test@example.com"
            val hashedPassword = passwordEncoder.encode(rawPassword)
            
            // Create verified user (verified = true)
            val verifiedUser = User(
                id = 1L,
                email = email,
                password = hashedPassword,
                verified = true
            )
            
            `when`(userRepository.findByEmail(email)).thenReturn(verifiedUser)
            `when`(jwtUtil.generateToken(any(), any())).thenReturn("mock-jwt-token")
            
            // Login should succeed and return JWT token
            val token = authService.login(email, rawPassword)
            
            token shouldNotBe null
            token.shouldNotBeBlank()
            token shouldBe "mock-jwt-token"
        }
    }
})
