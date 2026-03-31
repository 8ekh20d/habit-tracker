package app.web.bekh20d.habit_tracker.controller

import app.web.bekh20d.habit_tracker.dto.CreateHabitRequest
import app.web.bekh20d.habit_tracker.dto.LoginRequest
import app.web.bekh20d.habit_tracker.dto.SignupRequest
import app.web.bekh20d.habit_tracker.dto.UpdateHabitRequest
import app.web.bekh20d.habit_tracker.model.User
import app.web.bekh20d.habit_tracker.repository.HabitRepository
import app.web.bekh20d.habit_tracker.repository.UserRepository
import app.web.bekh20d.habit_tracker.service.AuthService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HabitControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var habitRepository: HabitRepository

    @Autowired
    private lateinit var authService: AuthService

    private lateinit var authToken: String

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
        habitRepository.deleteAll()

        // Create and verify a test user
        authService.signup("test@example.com", "password123")
        val user = userRepository.findByEmail("test@example.com")!!
        
        // Manually verify the user for testing
        val verifiedUser = User(
            id = user.id,
            email = user.email,
            password = user.password,
            verified = true,
            createdAt = user.createdAt
        )
        userRepository.save(verifiedUser)
        
        // Login to get auth token
        authToken = authService.login("test@example.com", "password123")
    }

    @Test
    fun `getHabits should return list of habits`() {
        // Create a habit first
        val createRequest = CreateHabitRequest(name = "Morning Exercise")
        mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )

        mockMvc.perform(
            get("/habits")
                .header("Authorization", "Bearer $authToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("Morning Exercise"))
            .andExpect(jsonPath("$[0].frequencyType").value("DAILY"))
    }

    @Test
    fun `createHabit should return created habit with 201 status`() {
        val request = CreateHabitRequest(name = "Morning Exercise")

        mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Morning Exercise"))
            .andExpect(jsonPath("$.frequencyType").value("DAILY"))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.createdAt").exists())
    }

    @Test
    fun `updateHabit should return updated habit`() {
        // Create a habit first
        val createRequest = CreateHabitRequest(name = "Morning Exercise")
        val createResult = mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        ).andReturn()

        val habitId = objectMapper.readTree(createResult.response.contentAsString).get("id").asLong()

        // Update the habit
        val updateRequest = UpdateHabitRequest(name = "Evening Exercise")
        mockMvc.perform(
            patch("/habits/{id}", habitId)
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(habitId))
            .andExpect(jsonPath("$.name").value("Evening Exercise"))
    }

    @Test
    fun `deleteHabit should return 204 No Content`() {
        // Create a habit first
        val createRequest = CreateHabitRequest(name = "Morning Exercise")
        val createResult = mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        ).andReturn()

        val habitId = objectMapper.readTree(createResult.response.contentAsString).get("id").asLong()

        // Delete the habit
        mockMvc.perform(
            delete("/habits/{id}", habitId)
                .header("Authorization", "Bearer $authToken")
        )
            .andExpect(status().isNoContent)

        // Verify it's deleted
        mockMvc.perform(
            get("/habits")
                .header("Authorization", "Bearer $authToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `createHabit with blank name should return 400 Bad Request`() {
        val request = CreateHabitRequest(name = "")

        mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
    }

    @Test
    fun `createHabit with name exceeding 100 characters should return 400 Bad Request`() {
        val longName = "a".repeat(101)
        val request = CreateHabitRequest(name = longName)

        mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
    }

    @Test
    fun `updateHabit for non-existent habit should return 404 Not Found`() {
        val updateRequest = UpdateHabitRequest(name = "Updated Name")

        mockMvc.perform(
            patch("/habits/{id}", 99999L)
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Habit not found or access denied"))
    }

    @Test
    fun `deleteHabit for non-existent habit should return 404 Not Found`() {
        mockMvc.perform(
            delete("/habits/{id}", 99999L)
                .header("Authorization", "Bearer $authToken")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Habit not found or access denied"))
    }

    @Test
    fun `getHabits without authentication should return 401 Unauthorized`() {
        mockMvc.perform(get("/habits"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `createHabit without authentication should return 401 Unauthorized`() {
        val request = CreateHabitRequest(name = "Morning Exercise")

        mockMvc.perform(
            post("/habits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `getHabits should return only user's own habits`() {
        // Create a habit for the first user
        val createRequest = CreateHabitRequest(name = "User1 Habit")
        mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )

        // Create a second user
        authService.signup("user2@example.com", "password123")
        val user2 = userRepository.findByEmail("user2@example.com")!!
        val verifiedUser2 = User(
            id = user2.id,
            email = user2.email,
            password = user2.password,
            verified = true,
            createdAt = user2.createdAt
        )
        userRepository.save(verifiedUser2)
        val authToken2 = authService.login("user2@example.com", "password123")

        // Create a habit for the second user
        val createRequest2 = CreateHabitRequest(name = "User2 Habit")
        mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $authToken2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest2))
        )

        // Verify first user only sees their own habit
        mockMvc.perform(
            get("/habits")
                .header("Authorization", "Bearer $authToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("User1 Habit"))

        // Verify second user only sees their own habit
        mockMvc.perform(
            get("/habits")
                .header("Authorization", "Bearer $authToken2")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("User2 Habit"))
    }

    @Test
    fun `updateHabit with name exceeding 100 characters should return 400 Bad Request`() {
        // Create a habit first
        val createRequest = CreateHabitRequest(name = "Morning Exercise")
        val createResult = mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        ).andReturn()

        val habitId = objectMapper.readTree(createResult.response.contentAsString).get("id").asLong()

        // Try to update with name exceeding 100 characters
        val longName = "a".repeat(101)
        val updateRequest = UpdateHabitRequest(name = longName)
        mockMvc.perform(
            patch("/habits/{id}", habitId)
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `updateHabit for habit not owned by user should return 404 Not Found`() {
        // Create a habit for the first user
        val createRequest = CreateHabitRequest(name = "User1 Habit")
        val createResult = mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        ).andReturn()

        val habitId = objectMapper.readTree(createResult.response.contentAsString).get("id").asLong()

        // Create a second user
        authService.signup("user2@example.com", "password123")
        val user2 = userRepository.findByEmail("user2@example.com")!!
        val verifiedUser2 = User(
            id = user2.id,
            email = user2.email,
            password = user2.password,
            verified = true,
            createdAt = user2.createdAt
        )
        userRepository.save(verifiedUser2)
        val authToken2 = authService.login("user2@example.com", "password123")

        // Try to update first user's habit with second user's token
        val updateRequest = UpdateHabitRequest(name = "Hacked Habit")
        mockMvc.perform(
            patch("/habits/{id}", habitId)
                .header("Authorization", "Bearer $authToken2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Habit not found or access denied"))
    }

    @Test
    fun `deleteHabit for habit not owned by user should return 404 Not Found`() {
        // Create a habit for the first user
        val createRequest = CreateHabitRequest(name = "User1 Habit")
        val createResult = mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        ).andReturn()

        val habitId = objectMapper.readTree(createResult.response.contentAsString).get("id").asLong()

        // Create a second user
        authService.signup("user2@example.com", "password123")
        val user2 = userRepository.findByEmail("user2@example.com")!!
        val verifiedUser2 = User(
            id = user2.id,
            email = user2.email,
            password = user2.password,
            verified = true,
            createdAt = user2.createdAt
        )
        userRepository.save(verifiedUser2)
        val authToken2 = authService.login("user2@example.com", "password123")

        // Try to delete first user's habit with second user's token
        mockMvc.perform(
            delete("/habits/{id}", habitId)
                .header("Authorization", "Bearer $authToken2")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Habit not found or access denied"))
    }
}
