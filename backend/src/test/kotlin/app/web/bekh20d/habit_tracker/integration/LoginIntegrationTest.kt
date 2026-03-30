package app.web.bekh20d.habit_tracker.integration

import app.web.bekh20d.habit_tracker.dto.LoginRequest
import app.web.bekh20d.habit_tracker.model.User
import app.web.bekh20d.habit_tracker.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoginIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
    }

    @Test
    fun `login with valid credentials and verified user should return 200 with JWT token`() {
        // Create a verified user
        val hashedPassword = passwordEncoder.encode("password123")
        val user = User(
            email = "test@example.com",
            password = hashedPassword,
            verified = true
        )
        userRepository.save(user)

        val loginRequest = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.accessToken").isNotEmpty)
    }

    @Test
    fun `login with invalid email should return 401 Unauthorized`() {
        val loginRequest = LoginRequest(
            email = "nonexistent@example.com",
            password = "password123"
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("Invalid email or password"))
    }

    @Test
    fun `login with unverified user should return 403 Forbidden`() {
        // Create an unverified user
        val hashedPassword = passwordEncoder.encode("password123")
        val user = User(
            email = "unverified@example.com",
            password = hashedPassword,
            verified = false
        )
        userRepository.save(user)

        val loginRequest = LoginRequest(
            email = "unverified@example.com",
            password = "password123"
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.message").value("Email not verified. Please check your email."))
    }

    @Test
    fun `login with invalid password should return 401 Unauthorized`() {
        // Create a verified user
        val hashedPassword = passwordEncoder.encode("correctpassword")
        val user = User(
            email = "test@example.com",
            password = hashedPassword,
            verified = true
        )
        userRepository.save(user)

        val loginRequest = LoginRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("Invalid email or password"))
    }

    @Test
    fun `login with invalid email format should return 400 Bad Request`() {
        val loginRequest = LoginRequest(
            email = "invalid-email",
            password = "password123"
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
    }

    @Test
    fun `login with blank email should return 400 Bad Request`() {
        val loginRequest = LoginRequest(
            email = "",
            password = "password123"
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
    }

    @Test
    fun `login with blank password should return 400 Bad Request`() {
        val loginRequest = LoginRequest(
            email = "test@example.com",
            password = ""
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
    }
}
