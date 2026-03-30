package app.web.bekh20d.habit_tracker.service

import app.web.bekh20d.habit_tracker.exception.InvalidCredentialsException
import app.web.bekh20d.habit_tracker.exception.UnverifiedUserException
import app.web.bekh20d.habit_tracker.model.User
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

@ExtendWith(MockitoExtension::class)
class AuthServiceLoginTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder
    
    @Mock
    private lateinit var jwtUtil: JwtUtil

    @InjectMocks
    private lateinit var authService: AuthService

    @Test
    fun `login should return JWT token for valid credentials and verified user`() {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val hashedPassword = "\$2a\$10\$hashedPasswordExample"
        val expectedToken = "jwt.token.here"
        
        val user = User(
            id = 1L,
            email = email,
            password = hashedPassword,
            verified = true
        )

        `when`(userRepository.findByEmail(email)).thenReturn(user)
        `when`(passwordEncoder.matches(password, hashedPassword)).thenReturn(true)
        `when`(jwtUtil.generateToken(user.id, user.email)).thenReturn(expectedToken)

        // Act
        val result = authService.login(email, password)

        // Assert
        assertEquals(expectedToken, result)
        verify(userRepository, times(1)).findByEmail(email)
        verify(passwordEncoder, times(1)).matches(password, hashedPassword)
        verify(jwtUtil, times(1)).generateToken(user.id, user.email)
    }

    @Test
    fun `login should throw InvalidCredentialsException when user not found`() {
        // Arrange
        val email = "nonexistent@example.com"
        val password = "password123"

        `when`(userRepository.findByEmail(email)).thenReturn(null)

        // Act & Assert
        val exception = assertThrows(InvalidCredentialsException::class.java) {
            authService.login(email, password)
        }
        
        assertEquals("Invalid email or password", exception.message)
        verify(userRepository, times(1)).findByEmail(email)
        verify(passwordEncoder, never()).matches(anyString(), anyString())
        verify(jwtUtil, never()).generateToken(anyLong(), anyString())
    }

    @Test
    fun `login should throw UnverifiedUserException when user is not verified`() {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val hashedPassword = "\$2a\$10\$hashedPasswordExample"
        
        val user = User(
            id = 1L,
            email = email,
            password = hashedPassword,
            verified = false
        )

        `when`(userRepository.findByEmail(email)).thenReturn(user)

        // Act & Assert
        val exception = assertThrows(UnverifiedUserException::class.java) {
            authService.login(email, password)
        }
        
        assertEquals("Email not verified. Please check your email.", exception.message)
        verify(userRepository, times(1)).findByEmail(email)
        verify(passwordEncoder, never()).matches(anyString(), anyString())
        verify(jwtUtil, never()).generateToken(anyLong(), anyString())
    }

    @Test
    fun `login should throw InvalidCredentialsException when password is incorrect`() {
        // Arrange
        val email = "test@example.com"
        val password = "wrongpassword"
        val hashedPassword = "\$2a\$10\$hashedPasswordExample"
        
        val user = User(
            id = 1L,
            email = email,
            password = hashedPassword,
            verified = true
        )

        `when`(userRepository.findByEmail(email)).thenReturn(user)
        `when`(passwordEncoder.matches(password, hashedPassword)).thenReturn(false)

        // Act & Assert
        val exception = assertThrows(InvalidCredentialsException::class.java) {
            authService.login(email, password)
        }
        
        assertEquals("Invalid email or password", exception.message)
        verify(userRepository, times(1)).findByEmail(email)
        verify(passwordEncoder, times(1)).matches(password, hashedPassword)
        verify(jwtUtil, never()).generateToken(anyLong(), anyString())
    }
}
