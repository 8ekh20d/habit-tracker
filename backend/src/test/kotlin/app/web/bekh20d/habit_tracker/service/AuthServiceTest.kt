package app.web.bekh20d.habit_tracker.service

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
class AuthServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder
    
    @Mock
    private lateinit var jwtUtil: JwtUtil

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

        // Act
        authService.signup(email, rawPassword)

        // Assert
        verify(passwordEncoder, times(1)).encode(rawPassword)
    }
}
