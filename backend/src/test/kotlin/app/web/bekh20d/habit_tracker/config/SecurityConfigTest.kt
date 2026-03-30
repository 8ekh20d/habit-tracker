package app.web.bekh20d.habit_tracker.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Test
    fun `passwordEncoder bean is created`() {
        assertNotNull(passwordEncoder)
    }

    @Test
    fun `passwordEncoder hashes passwords using BCrypt`() {
        // Given
        val rawPassword = "myPassword123"

        // When
        val hashedPassword = passwordEncoder.encode(rawPassword)

        // Then
        assertNotEquals(rawPassword, hashedPassword)
        assertTrue(hashedPassword?.startsWith("\$2a\$") == true || hashedPassword?.startsWith("\$2b\$") == true)
        assertTrue(passwordEncoder.matches(rawPassword, hashedPassword))
    }

    @Test
    fun `passwordEncoder generates unique hashes for same password`() {
        // Given
        val rawPassword = "myPassword123"

        // When
        val hash1 = passwordEncoder.encode(rawPassword)
        val hash2 = passwordEncoder.encode(rawPassword)

        // Then
        assertNotEquals(hash1, hash2)
        assertTrue(passwordEncoder.matches(rawPassword, hash1))
        assertTrue(passwordEncoder.matches(rawPassword, hash2))
    }

    @Test
    fun `passwordEncoder uses BCrypt strength factor 10 or higher`() {
        // Given
        val rawPassword = "testPassword"

        // When
        val hashedPassword = passwordEncoder.encode(rawPassword)

        // Then - BCrypt format: $2a$10$... where 10 is the strength factor
        val strengthFactor = hashedPassword?.substring(4, 6)?.toInt() ?: 0
        assertTrue(strengthFactor >= 10, "BCrypt strength factor should be 10 or higher, but was $strengthFactor")
    }
}
