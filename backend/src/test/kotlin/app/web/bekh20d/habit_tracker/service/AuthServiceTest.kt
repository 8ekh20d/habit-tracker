package app.web.bekh20d.habit_tracker.service

import app.web.bekh20d.habit_tracker.exception.InvalidTokenException
import app.web.bekh20d.habit_tracker.model.EmailVerificationToken
import app.web.bekh20d.habit_tracker.model.User
import app.web.bekh20d.habit_tracker.repository.EmailVerificationTokenRepository
import app.web.bekh20d.habit_tracker.repository.UserRepository
import app.web.bekh20d.habit_tracker.util.JwtUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var emailVerificationTokenRepository: EmailVerificationTokenRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder
    
    @Mock
    private lateinit var jwtUtil: JwtUtil

    @Mock
    private lateinit var emailService: EmailService

    @InjectMocks
    private lateinit var authService: AuthService

    @Test
    fun `signup should create user with hashed password and verified false`() {
        // Arrange
        val email = "test@example.com"
        val rawPassword = "password123"
        val hashedPassword = "\$2a\$10\$hashedPasswordExample"

        `when`(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword)
        `when`(userRepository.save(any(User::class.java))).thenAnswer { invocation ->
            val user = invocation.getArgument<User>(0)
            User(
                id = 1L,
                email = user.email,
                password = user.password,
                verified = user.verified,
                createdAt = user.createdAt
            )
        }
        `when`(emailVerificationTokenRepository.save(any(EmailVerificationToken::class.java))).thenAnswer { invocation ->
            invocation.getArgument(0)
        }

        // Act
        val result = authService.signup(email, rawPassword)

        // Assert
        assertNotNull(result)
        assertEquals(email, result.email)
        assertEquals(hashedPassword, result.password)
        assertFalse(result.verified)
        assertNotEquals(rawPassword, result.password)

        verify(passwordEncoder, times(1)).encode(rawPassword)
        verify(userRepository, times(1)).save(any(User::class.java))
    }

    @Test
    fun `signup should hash password using BCrypt`() {
        // Arrange
        val email = "test@example.com"
        val rawPassword = "securePassword123"
        val hashedPassword = "\$2a\$10\$anotherHashedPassword"

        `when`(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword)
        `when`(userRepository.save(any(User::class.java))).thenAnswer { invocation ->
            invocation.getArgument(0)
        }
        `when`(emailVerificationTokenRepository.save(any(EmailVerificationToken::class.java))).thenAnswer { invocation ->
            invocation.getArgument(0)
        }

        // Act
        authService.signup(email, rawPassword)

        // Assert
        verify(passwordEncoder, times(1)).encode(rawPassword)
    }

    @Test
    fun `verifyEmail should update user verified status to true with valid token`() {
        // Arrange
        val token = "valid-token-123"
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

        `when`(emailVerificationTokenRepository.findByToken(token)).thenReturn(verificationToken)
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        `when`(userRepository.save(any(User::class.java))).thenAnswer { invocation -> invocation.getArgument(0) }

        // Act
        authService.verifyEmail(token)

        // Assert
        verify(emailVerificationTokenRepository, times(1)).findByToken(token)
        verify(userRepository, times(1)).findById(userId)
        verify(userRepository, times(1)).save(any(User::class.java))
        verify(emailVerificationTokenRepository, times(1)).delete(verificationToken)
    }

    @Test
    fun `verifyEmail should throw InvalidTokenException with expired token`() {
        // Arrange
        val token = "expired-token-123"
        val userId = 1L
        val verificationToken = EmailVerificationToken(
            id = 1L,
            userId = userId,
            token = token,
            expiryDate = LocalDateTime.now().minusHours(1) // Expired 1 hour ago
        )

        `when`(emailVerificationTokenRepository.findByToken(token)).thenReturn(verificationToken)

        // Act & Assert
        assertThrows(InvalidTokenException::class.java) {
            authService.verifyEmail(token)
        }

        verify(emailVerificationTokenRepository, times(1)).findByToken(token)
        verify(emailVerificationTokenRepository, times(1)).delete(verificationToken)
        verify(userRepository, never()).save(any(User::class.java))
    }

    @Test
    fun `verifyEmail should throw InvalidTokenException with invalid token`() {
        // Arrange
        val token = "invalid-token-123"

        `when`(emailVerificationTokenRepository.findByToken(token)).thenReturn(null)

        // Act & Assert
        assertThrows(InvalidTokenException::class.java) {
            authService.verifyEmail(token)
        }

        verify(emailVerificationTokenRepository, times(1)).findByToken(token)
        verify(userRepository, never()).save(any(User::class.java))
    }

    @Test
    fun `verifyEmail should delete token after successful verification`() {
        // Arrange
        val token = "valid-token-123"
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

        `when`(emailVerificationTokenRepository.findByToken(token)).thenReturn(verificationToken)
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        `when`(userRepository.save(any(User::class.java))).thenAnswer { invocation -> invocation.getArgument(0) }

        // Act
        authService.verifyEmail(token)

        // Assert
        verify(emailVerificationTokenRepository, times(1)).delete(verificationToken)
    }
}
