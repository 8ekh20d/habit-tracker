package app.web.bekh20d.habit_tracker.service

import app.web.bekh20d.habit_tracker.exception.InvalidTokenException
import app.web.bekh20d.habit_tracker.model.EmailVerificationToken
import app.web.bekh20d.habit_tracker.model.User
import app.web.bekh20d.habit_tracker.repository.EmailVerificationTokenRepository
import app.web.bekh20d.habit_tracker.repository.UserRepository
import app.web.bekh20d.habit_tracker.util.JwtUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.*

/**
 * Property-Based Tests for Verification Token Cleanup
 * 
 * **Validates: Requirements 2.4**
 * 
 * Property 10: Verification Token Cleanup
 * - Tokens are deleted after successful verification
 * - Tokens cannot be reused after verification
 */
class AuthServiceVerificationTokenCleanupPropertyTest : StringSpec({

    "Property 10: tokens are deleted after successful verification" {
        val userRepository = mock(UserRepository::class.java)
        val tokenRepository = mock(EmailVerificationTokenRepository::class.java)
        val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()
        val jwtUtil = mock(JwtUtil::class.java)
        val emailService = mock(EmailService::class.java)
        val authService = AuthService(userRepository, tokenRepository, passwordEncoder, jwtUtil, emailService)

        checkAll(5, Arb.string(10..50)) { token ->
            val userId = 1L
            
            val verificationToken = EmailVerificationToken(
                id = 1L,
                userId = userId,
                token = token,
                expiryDate = LocalDateTime.now().plusHours(24)
            )
            
            val user = User(
                id = userId,
                email = "test@example.com",
                password = "hashedPassword",
                verified = false
            )
            
            val verifiedUser = User(
                id = userId,
                email = "test@example.com",
                password = "hashedPassword",
                verified = true
            )
            
            `when`(tokenRepository.findByToken(token)).thenReturn(verificationToken)
            `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
            `when`(userRepository.save(any())).thenReturn(verifiedUser)
            
            // Verify email
            authService.verifyEmail(token)
            
            // Verify that token was deleted
            verify(tokenRepository, times(1)).delete(verificationToken)
        }
    }

    "Property 10: tokens cannot be reused after verification" {
        val userRepository = mock(UserRepository::class.java)
        val tokenRepository = mock(EmailVerificationTokenRepository::class.java)
        val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()
        val jwtUtil = mock(JwtUtil::class.java)
        val emailService = mock(EmailService::class.java)
        val authService = AuthService(userRepository, tokenRepository, passwordEncoder, jwtUtil, emailService)

        checkAll(5, Arb.string(10..50)) { token ->
            val userId = 1L
            
            val verificationToken = EmailVerificationToken(
                id = 1L,
                userId = userId,
                token = token,
                expiryDate = LocalDateTime.now().plusHours(24)
            )
            
            val user = User(
                id = userId,
                email = "test@example.com",
                password = "hashedPassword",
                verified = false
            )
            
            val verifiedUser = User(
                id = userId,
                email = "test@example.com",
                password = "hashedPassword",
                verified = true
            )
            
            // First verification succeeds
            `when`(tokenRepository.findByToken(token)).thenReturn(verificationToken)
            `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
            `when`(userRepository.save(any())).thenReturn(verifiedUser)
            
            authService.verifyEmail(token)
            
            // Second attempt should fail (token not found)
            `when`(tokenRepository.findByToken(token)).thenReturn(null)
            
            shouldThrow<InvalidTokenException> {
                authService.verifyEmail(token)
            }
        }
    }
})
