package app.web.bekh20d.habit_tracker.integration

import app.web.bekh20d.habit_tracker.dto.*
import app.web.bekh20d.habit_tracker.model.FrequencyType
import app.web.bekh20d.habit_tracker.model.Habit
import app.web.bekh20d.habit_tracker.model.User
import app.web.bekh20d.habit_tracker.repository.EmailVerificationTokenRepository
import app.web.bekh20d.habit_tracker.repository.HabitRecordRepository
import app.web.bekh20d.habit_tracker.repository.HabitRepository
import app.web.bekh20d.habit_tracker.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate

/**
 * Integration Test for Authorization Enforcement
 * 
 * **Validates: Requirements 15.1, 15.2, 15.3, 15.5**
 * 
 * This test verifies that users can only access, modify, and delete their own habits.
 * It ensures proper authorization enforcement across all habit operations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthorizationEnforcementIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var emailVerificationTokenRepository: EmailVerificationTokenRepository

    @Autowired
    private lateinit var habitRepository: HabitRepository

    @Autowired
    private lateinit var habitRecordRepository: HabitRecordRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var userAToken: String
    private lateinit var userBToken: String
    private var userAHabitId: Long = 0
    private var userBHabitId: Long = 0

    @BeforeEach
    fun setup() {
        // Create User A
        val userA = User(
            email = "userA@example.com",
            password = passwordEncoder.encode("password123"),
            verified = true
        )
        val savedUserA = userRepository.save(userA)

        // Create User B
        val userB = User(
            email = "userB@example.com",
            password = passwordEncoder.encode("password123"),
            verified = true
        )
        val savedUserB = userRepository.save(userB)

        // Login User A
        val loginRequestA = LoginRequest(email = "userA@example.com", password = "password123")
        val loginResponseA = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestA))
        ).andReturn()
        val loginDataA = objectMapper.readValue(
            loginResponseA.response.contentAsString,
            LoginResponse::class.java
        )
        userAToken = loginDataA.accessToken

        // Login User B
        val loginRequestB = LoginRequest(email = "userB@example.com", password = "password123")
        val loginResponseB = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestB))
        ).andReturn()
        val loginDataB = objectMapper.readValue(
            loginResponseB.response.contentAsString,
            LoginResponse::class.java
        )
        userBToken = loginDataB.accessToken

        // Create habit for User A
        val habitA = Habit(
            userId = savedUserA.id,
            name = "User A Habit",
            frequencyType = FrequencyType.DAILY
        )
        val savedHabitA = habitRepository.save(habitA)
        userAHabitId = savedHabitA.id

        // Create habit for User B
        val habitB = Habit(
            userId = savedUserB.id,
            name = "User B Habit",
            frequencyType = FrequencyType.DAILY
        )
        val savedHabitB = habitRepository.save(habitB)
        userBHabitId = savedHabitB.id
    }

    @AfterEach
    fun cleanup() {
        habitRecordRepository.deleteAll()
        habitRepository.deleteAll()
        emailVerificationTokenRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `user A cannot access user B's habits in list`() {
        // User A gets their habits
        val responseA = mockMvc.perform(
            get("/habits")
                .header("Authorization", "Bearer $userAToken")
        )
            .andExpect(status().isOk)
            .andReturn()

        val habitsA = objectMapper.readValue(
            responseA.response.contentAsString,
            Array<HabitResponse>::class.java
        )

        // Verify User A only sees their own habit
        assert(habitsA.size == 1)
        assert(habitsA[0].id == userAHabitId)
        assert(habitsA[0].name == "User A Habit")

        // User B gets their habits
        val responseB = mockMvc.perform(
            get("/habits")
                .header("Authorization", "Bearer $userBToken")
        )
            .andExpect(status().isOk)
            .andReturn()

        val habitsB = objectMapper.readValue(
            responseB.response.contentAsString,
            Array<HabitResponse>::class.java
        )

        // Verify User B only sees their own habit
        assert(habitsB.size == 1)
        assert(habitsB[0].id == userBHabitId)
        assert(habitsB[0].name == "User B Habit")
    }

    @Test
    fun `user A cannot modify user B's habits`() {
        // User A attempts to update User B's habit
        val updateRequest = UpdateHabitRequest(name = "Hacked Habit Name")
        
        mockMvc.perform(
            patch("/habits/$userBHabitId")
                .header("Authorization", "Bearer $userAToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)

        // Verify User B's habit was not modified
        val habitB = habitRepository.findById(userBHabitId).orElseThrow()
        assert(habitB.name == "User B Habit")
    }

    @Test
    fun `user A cannot delete user B's habits`() {
        // User A attempts to delete User B's habit
        mockMvc.perform(
            delete("/habits/$userBHabitId")
                .header("Authorization", "Bearer $userAToken")
        )
            .andExpect(status().isNotFound)

        // Verify User B's habit still exists
        val habitExists = habitRepository.existsById(userBHabitId)
        assert(habitExists)
    }

    @Test
    fun `user A cannot check user B's habits`() {
        // User A attempts to check User B's habit
        val checkRequest = CheckHabitRequest(date = LocalDate.now())
        
        mockMvc.perform(
            post("/habits/$userBHabitId/check")
                .header("Authorization", "Bearer $userAToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkRequest))
        )
            .andExpect(status().isNotFound)

        // Verify no record was created for User B's habit
        val records = habitRecordRepository.findByHabitIdOrderByDateDesc(userBHabitId)
        assert(records.isEmpty())
    }

    @Test
    fun `user can only modify their own habits`() {
        // User A updates their own habit - should succeed
        val updateRequest = UpdateHabitRequest(name = "Updated User A Habit")
        
        mockMvc.perform(
            patch("/habits/$userAHabitId")
                .header("Authorization", "Bearer $userAToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated User A Habit"))

        // Verify habit was updated
        val habitA = habitRepository.findById(userAHabitId).orElseThrow()
        assert(habitA.name == "Updated User A Habit")
    }

    @Test
    fun `user can only delete their own habits`() {
        // User B deletes their own habit - should succeed
        mockMvc.perform(
            delete("/habits/$userBHabitId")
                .header("Authorization", "Bearer $userBToken")
        )
            .andExpect(status().isNoContent)

        // Verify habit was deleted
        val habitExists = habitRepository.existsById(userBHabitId)
        assert(!habitExists)
    }

    @Test
    fun `user can only check their own habits`() {
        // User A checks their own habit - should succeed
        val checkRequest = CheckHabitRequest(date = LocalDate.now())
        
        mockMvc.perform(
            post("/habits/$userAHabitId/check")
                .header("Authorization", "Bearer $userAToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.habitId").value(userAHabitId))

        // Verify record was created
        val records = habitRecordRepository.findByHabitIdOrderByDateDesc(userAHabitId)
        assert(records.size == 1)
    }

    @Test
    fun `stats only include user's own habits`() {
        // Create records for both users
        val checkRequest = CheckHabitRequest(date = LocalDate.now())
        
        mockMvc.perform(
            post("/habits/$userAHabitId/check")
                .header("Authorization", "Bearer $userAToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkRequest))
        )

        mockMvc.perform(
            post("/habits/$userBHabitId/check")
                .header("Authorization", "Bearer $userBToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkRequest))
        )

        // User A gets stats
        val statsResponseA = mockMvc.perform(
            get("/stats")
                .header("Authorization", "Bearer $userAToken")
        )
            .andExpect(status().isOk)
            .andReturn()

        val statsA = objectMapper.readValue(
            statsResponseA.response.contentAsString,
            StatsResponse::class.java
        )

        // Verify User A only sees their own habit stats
        assert(statsA.habits.size == 1)
        assert(statsA.habits[0].habitId == userAHabitId)
        assert(statsA.habits[0].habitName == "User A Habit")

        // User B gets stats
        val statsResponseB = mockMvc.perform(
            get("/stats")
                .header("Authorization", "Bearer $userBToken")
        )
            .andExpect(status().isOk)
            .andReturn()

        val statsB = objectMapper.readValue(
            statsResponseB.response.contentAsString,
            StatsResponse::class.java
        )

        // Verify User B only sees their own habit stats
        assert(statsB.habits.size == 1)
        assert(statsB.habits[0].habitId == userBHabitId)
        assert(statsB.habits[0].habitName == "User B Habit")
    }

    @Test
    fun `unauthorized requests are rejected`() {
        // Test all endpoints without authentication
        mockMvc.perform(get("/habits"))
            .andExpect(status().isUnauthorized)

        mockMvc.perform(
            post("/habits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateHabitRequest("Test")))
        )
            .andExpect(status().isUnauthorized)

        mockMvc.perform(
            patch("/habits/$userAHabitId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(UpdateHabitRequest("Test")))
        )
            .andExpect(status().isUnauthorized)

        mockMvc.perform(delete("/habits/$userAHabitId"))
            .andExpect(status().isUnauthorized)

        mockMvc.perform(
            post("/habits/$userAHabitId/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CheckHabitRequest(LocalDate.now())))
        )
            .andExpect(status().isUnauthorized)

        mockMvc.perform(get("/stats"))
            .andExpect(status().isUnauthorized)
    }
}
