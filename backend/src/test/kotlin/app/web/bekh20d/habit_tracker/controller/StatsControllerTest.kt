package app.web.bekh20d.habit_tracker.controller

import app.web.bekh20d.habit_tracker.dto.CheckHabitRequest
import app.web.bekh20d.habit_tracker.dto.CreateHabitRequest
import app.web.bekh20d.habit_tracker.model.User
import app.web.bekh20d.habit_tracker.repository.HabitRecordRepository
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
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StatsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var habitRepository: HabitRepository

    @Autowired
    private lateinit var habitRecordRepository: HabitRecordRepository

    @Autowired
    private lateinit var authService: AuthService

    private lateinit var authToken: String

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
        habitRepository.deleteAll()
        habitRecordRepository.deleteAll()

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
    fun `getStats should return data for all user habits`() {
        // Create multiple habits
        val habit1Request = CreateHabitRequest(name = "Morning Exercise")
        val habit1Result = mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(habit1Request))
        ).andReturn()
        val habit1Id = objectMapper.readTree(habit1Result.response.contentAsString).get("id").asLong()

        val habit2Request = CreateHabitRequest(name = "Read Books")
        val habit2Result = mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(habit2Request))
        ).andReturn()
        val habit2Id = objectMapper.readTree(habit2Result.response.contentAsString).get("id").asLong()

        val habit3Request = CreateHabitRequest(name = "Meditation")
        mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(habit3Request))
        )

        // Check some habits
        mockMvc.perform(
            post("/habits/{id}/check", habit1Id)
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CheckHabitRequest(date = LocalDate.now())))
        )

        mockMvc.perform(
            post("/habits/{id}/check", habit2Id)
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CheckHabitRequest(date = LocalDate.now())))
        )

        // Get stats
        mockMvc.perform(
            get("/stats")
                .header("Authorization", "Bearer $authToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.habits.length()").value(3))
            .andExpect(jsonPath("$.habits[?(@.habitName == 'Morning Exercise')]").exists())
            .andExpect(jsonPath("$.habits[?(@.habitName == 'Read Books')]").exists())
            .andExpect(jsonPath("$.habits[?(@.habitName == 'Meditation')]").exists())
    }

    @Test
    fun `getStats should include correct streak and total completions`() {
        // Create a habit
        val habitRequest = CreateHabitRequest(name = "Morning Exercise")
        val habitResult = mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(habitRequest))
        ).andReturn()
        val habitId = objectMapper.readTree(habitResult.response.contentAsString).get("id").asLong()

        // Check habit for consecutive days
        val today = LocalDate.now()
        mockMvc.perform(
            post("/habits/{id}/check", habitId)
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CheckHabitRequest(date = today)))
        )

        mockMvc.perform(
            post("/habits/{id}/check", habitId)
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CheckHabitRequest(date = today.minusDays(1))))
        )

        mockMvc.perform(
            post("/habits/{id}/check", habitId)
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CheckHabitRequest(date = today.minusDays(2))))
        )

        // Check habit for a non-consecutive day (creates a gap)
        mockMvc.perform(
            post("/habits/{id}/check", habitId)
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CheckHabitRequest(date = today.minusDays(5))))
        )

        // Get stats
        mockMvc.perform(
            get("/stats")
                .header("Authorization", "Bearer $authToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.habits.length()").value(1))
            .andExpect(jsonPath("$.habits[0].habitName").value("Morning Exercise"))
            .andExpect(jsonPath("$.habits[0].currentStreak").value(3)) // Consecutive days: today, yesterday, day before
            .andExpect(jsonPath("$.habits[0].totalCompletions").value(4)) // Total records: 4
            .andExpect(jsonPath("$.habits[0].habitId").value(habitId))
    }

    @Test
    fun `getStats without authentication should return 401 Unauthorized`() {
        mockMvc.perform(get("/stats"))
            .andExpect(status().isUnauthorized)
    }
}
