package app.web.bekh20d.habit_tracker.repository

import app.web.bekh20d.habit_tracker.model.User
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `findByEmail returns user when email exists`() {
        // Given
        val user = User(
            email = "test@example.com",
            password = "hashedPassword123",
            verified = false
        )
        entityManager.persist(user)
        entityManager.flush()

        // When
        val foundUser = userRepository.findByEmail("test@example.com")

        // Then
        assertNotNull(foundUser)
        assertEquals("test@example.com", foundUser.email)
        assertEquals("hashedPassword123", foundUser.password)
    }

    @Test
    fun `findByEmail returns null when email does not exist`() {
        // When
        val foundUser = userRepository.findByEmail("nonexistent@example.com")

        // Then
        assertNull(foundUser)
    }
}
