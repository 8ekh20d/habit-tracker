package app.web.bekh20d.habit_tracker.service

import app.web.bekh20d.habit_tracker.repository.EmailVerificationTokenRepository
import app.web.bekh20d.habit_tracker.repository.UserRepository
import app.web.bekh20d.habit_tracker.util.JwtUtil
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.shouldStartWith
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Property-Based Tests for Password Hashing
 * 
 * **Validates: Requirements 1.2, 1.8, 11.1**
 * 
 * Property 1: Password Security
 * - Passwords are never stored in plaintext
 * - BCrypt hashes are always 60 characters
 * - Same password produces different hashes (Property 8)
 */
class AuthServicePasswordPropertyTest : StringSpec({

    "Property 1: passwords are never stored in plaintext" {
        val userRepository = mock(UserRepository::class.java)
        val tokenRepository = mock(EmailVerificationTokenRepository::class.java)
        val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()
        val jwtUtil = mock(JwtUtil::class.java)
        val emailService = mock(EmailService::class.java)
        val authService = AuthService(userRepository, tokenRepository, passwordEncoder, jwtUtil, emailService)

        `when`(userRepository.save(any())).thenAnswer { invocation -> invocation.getArgument(0) }
        `when`(tokenRepository.save(any())).thenAnswer { invocation -> invocation.getArgument(0) }

        checkAll(5, Arb.string(8..100)) { rawPassword ->
            val user = authService.signup("test@example.com", rawPassword)
            
            // Password should never be stored in plaintext
            user.password shouldNotBe rawPassword
        }
    }

    "Property 1: BCrypt hashes are always 60 characters" {
        val userRepository = mock(UserRepository::class.java)
        val tokenRepository = mock(EmailVerificationTokenRepository::class.java)
        val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()
        val jwtUtil = mock(JwtUtil::class.java)
        val emailService = mock(EmailService::class.java)
        val authService = AuthService(userRepository, tokenRepository, passwordEncoder, jwtUtil, emailService)

        `when`(userRepository.save(any())).thenAnswer { invocation -> invocation.getArgument(0) }
        `when`(tokenRepository.save(any())).thenAnswer { invocation -> invocation.getArgument(0) }

        checkAll(5, Arb.string(8..100)) { rawPassword ->
            val user = authService.signup("test@example.com", rawPassword)
            
            // BCrypt hashes are always 60 characters
            user.password shouldHaveLength 60
        }
    }

    "Property 1: BCrypt hashes start with correct prefix" {
        val userRepository = mock(UserRepository::class.java)
        val tokenRepository = mock(EmailVerificationTokenRepository::class.java)
        val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()
        val jwtUtil = mock(JwtUtil::class.java)
        val emailService = mock(EmailService::class.java)
        val authService = AuthService(userRepository, tokenRepository, passwordEncoder, jwtUtil, emailService)

        `when`(userRepository.save(any())).thenAnswer { invocation -> invocation.getArgument(0) }
        `when`(tokenRepository.save(any())).thenAnswer { invocation -> invocation.getArgument(0) }

        checkAll(5, Arb.string(8..100)) { rawPassword ->
            val user = authService.signup("test@example.com", rawPassword)
            
            // BCrypt hashes start with $2a$ or $2b$
            val startsWithValidPrefix = user.password.startsWith("\$2a\$") || user.password.startsWith("\$2b\$")
            startsWithValidPrefix shouldBe true
        }
    }

    "Property 8: same password produces different hashes due to unique salts" {
        val userRepository = mock(UserRepository::class.java)
        val tokenRepository = mock(EmailVerificationTokenRepository::class.java)
        val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()
        val jwtUtil = mock(JwtUtil::class.java)
        val emailService = mock(EmailService::class.java)
        val authService = AuthService(userRepository, tokenRepository, passwordEncoder, jwtUtil, emailService)

        `when`(userRepository.save(any())).thenAnswer { invocation -> invocation.getArgument(0) }
        `when`(tokenRepository.save(any())).thenAnswer { invocation -> invocation.getArgument(0) }

        checkAll(5, Arb.string(8..100)) { rawPassword ->
            val user1 = authService.signup("test1@example.com", rawPassword)
            val user2 = authService.signup("test2@example.com", rawPassword)
            
            // Same password should produce different hashes
            user1.password shouldNotBe user2.password
            
            // But both hashes should match the original password
            passwordEncoder.matches(rawPassword, user1.password) shouldBe true
            passwordEncoder.matches(rawPassword, user2.password) shouldBe true
        }
    }
})
