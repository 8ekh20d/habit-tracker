package app.web.bekh20d.habit_tracker.controller

import app.web.bekh20d.habit_tracker.dto.SignupRequest
import app.web.bekh20d.habit_tracker.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
    }

    @Test
    fun `signup with valid data should return 201 Created`() {
        val signupRequest = SignupRequest(
            email = "test@example.com",
            password = "password123"
        )

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `signup with duplicate email should return 409 Conflict`() {
        val signupRequest = SignupRequest(
            email = "duplicate@example.com",
            password = "password123"
        )

        // First signup
        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )
            .andExpect(status().isCreated)

        // Second signup with same email
        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("Email already registered"))
    }

    @Test
    fun `signup with invalid email should return 400 Bad Request`() {
        val signupRequest = SignupRequest(
            email = "invalid-email",
            password = "password123"
        )

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
    }

    @Test
    fun `signup with short password should return 400 Bad Request`() {
        val signupRequest = SignupRequest(
            email = "test@example.com",
            password = "short"
        )

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors[0]").value("Password must be at least 8 characters"))
    }

    @Test
    fun `signup with blank email should return 400 Bad Request`() {
        val signupRequest = SignupRequest(
            email = "",
            password = "password123"
        )

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
    }

    @Test
    fun `signup should store hashed password not plaintext`() {
        val signupRequest = SignupRequest(
            email = "secure@example.com",
            password = "mySecurePassword123"
        )

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )
            .andExpect(status().isCreated)

        val user = userRepository.findByEmail("secure@example.com")
        assert(user != null)
        assert(user!!.password != "mySecurePassword123")
        assert(user.password.startsWith("\$2a\$") || user.password.startsWith("\$2b\$"))
    }
}
