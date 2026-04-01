package app.web.bekh20d.habit_tracker.integration

import app.web.bekh20d.habit_tracker.dto.*
import app.web.bekh20d.habit_tracker.model.FrequencyType
import app.web.bekh20d.habit_tracker.repository.EmailVerificationTokenRepository
import app.web.bekh20d.habit_tracker.repository.HabitRecordRepository
import app.web.bekh20d.habit_tracker.repository.HabitRepository
import app.web.bekh20d.habit_tracker.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate

/**
 * Integration Test for Complete User Journey
 * 
 * **Validates: Requirements 1.1, 2.1, 3.1, 4.1, 8.1, 10.1**
 * 
 * This test verifies the complete end-to-end flow:
 * signup → verify email → login → create habit → check habit → get stats
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompleteUserJourneyIntegrationTest {

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

    @AfterEach
    fun cleanup() {
        habitRecordRepository.deleteAll()
        habitRepository.deleteAll()
        emailVerificationTokenRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `complete user journey from signup to stats`() {
        val testEmail = "journey@example.com"
        val testPassword = "securePassword123"
        val habitName = "Morning Exercise"

        // Step 1: Signup
        val signupRequest = SignupRequest(email = testEmail, password = testPassword)
        val signupResponse = mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.message").exists())
            .andReturn()

        val signupMessage = objectMapper.readValue(
            signupResponse.response.contentAsString,
            MessageResponse::class.java
        )
        assertTrue(signupMessage.message.contains("registered successfully"))

        // Verify user was created with verified=false
        val user = userRepository.findByEmail(testEmail)
        assertNotNull(user)
        assertFalse(user!!.verified)

        // Step 2: Verify Email
        val verificationToken = emailVerificationTokenRepository.findAll().first()
        assertNotNull(verificationToken)
        assertEquals(user.id, verificationToken.userId)

        mockMvc.perform(
            post("/auth/verify-email")
                .param("token", verificationToken.token)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Email verified successfully. You can now login."))

        // Verify user is now verified
        val verifiedUser = userRepository.findByEmail(testEmail)
        assertTrue(verifiedUser!!.verified)

        // Verify token was deleted
        val deletedToken = emailVerificationTokenRepository.findByToken(verificationToken.token)
        assertNull(deletedToken)

        // Step 3: Login
        val loginRequest = LoginRequest(email = testEmail, password = testPassword)
        val loginResponse = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andReturn()

        val loginData = objectMapper.readValue(
            loginResponse.response.contentAsString,
            LoginResponse::class.java
        )
        val jwtToken = loginData.accessToken
        assertNotNull(jwtToken)
        assertTrue(jwtToken.isNotEmpty())

        // Step 4: Create Habit (authenticated request)
        val createHabitRequest = CreateHabitRequest(name = habitName)
        val createHabitResponse = mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createHabitRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value(habitName))
            .andExpect(jsonPath("$.frequencyType").value("DAILY"))
            .andReturn()

        val habitResponse = objectMapper.readValue(
            createHabitResponse.response.contentAsString,
            HabitResponse::class.java
        )
        val habitId = habitResponse.id

        // Step 5: Check Habit (mark as done for today)
        val today = LocalDate.now()
        val checkHabitRequest = CheckHabitRequest(date = today)
        mockMvc.perform(
            post("/habits/$habitId/check")
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkHabitRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.habitId").value(habitId))
            .andExpect(jsonPath("$.date").value(today.toString()))
            .andExpect(jsonPath("$.status").value("DONE"))

        // Check habit for yesterday
        val yesterday = today.minusDays(1)
        val checkYesterdayRequest = CheckHabitRequest(date = yesterday)
        mockMvc.perform(
            post("/habits/$habitId/check")
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkYesterdayRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.date").value(yesterday.toString()))

        // Check habit for day before yesterday
        val dayBeforeYesterday = today.minusDays(2)
        val checkDayBeforeRequest = CheckHabitRequest(date = dayBeforeYesterday)
        mockMvc.perform(
            post("/habits/$habitId/check")
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkDayBeforeRequest))
        )
            .andExpect(status().isOk)

        // Step 6: Get Stats
        val statsResponse = mockMvc.perform(
            get("/stats")
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.habits").isArray)
            .andExpect(jsonPath("$.habits[0].habitId").value(habitId))
            .andExpect(jsonPath("$.habits[0].habitName").value(habitName))
            .andExpect(jsonPath("$.habits[0].currentStreak").value(3))
            .andExpect(jsonPath("$.habits[0].totalCompletions").value(3))
            .andReturn()

        val stats = objectMapper.readValue(
            statsResponse.response.contentAsString,
            StatsResponse::class.java
        )
        
        assertEquals(1, stats.habits.size)
        assertEquals(habitId, stats.habits[0].habitId)
        assertEquals(habitName, stats.habits[0].habitName)
        assertEquals(3, stats.habits[0].currentStreak)
        assertEquals(3, stats.habits[0].totalCompletions)
    }

    @Test
    fun `JWT token works for authenticated requests`() {
        // Setup: Create and verify user
        val testEmail = "jwt@example.com"
        val testPassword = "password123"
        
        val signupRequest = SignupRequest(email = testEmail, password = testPassword)
        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )

        val token = emailVerificationTokenRepository.findAll().first()
        mockMvc.perform(post("/auth/verify-email").param("token", token.token))

        val loginRequest = LoginRequest(email = testEmail, password = testPassword)
        val loginResponse = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        ).andReturn()

        val loginData = objectMapper.readValue(
            loginResponse.response.contentAsString,
            LoginResponse::class.java
        )
        val jwtToken = loginData.accessToken

        // Test: Authenticated request succeeds
        mockMvc.perform(
            get("/habits")
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(status().isOk)

        // Test: Request without JWT fails
        mockMvc.perform(get("/habits"))
            .andExpect(status().isUnauthorized)

        // Test: Request with invalid JWT fails
        mockMvc.perform(
            get("/habits")
                .header("Authorization", "Bearer invalid-token")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `streak calculation is correct after checking habits`() {
        // Setup: Create verified user and login
        val testEmail = "streak@example.com"
        val testPassword = "password123"
        
        val signupRequest = SignupRequest(email = testEmail, password = testPassword)
        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )

        val token = emailVerificationTokenRepository.findAll().first()
        mockMvc.perform(post("/auth/verify-email").param("token", token.token))

        val loginRequest = LoginRequest(email = testEmail, password = testPassword)
        val loginResponse = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        ).andReturn()

        val loginData = objectMapper.readValue(
            loginResponse.response.contentAsString,
            LoginResponse::class.java
        )
        val jwtToken = loginData.accessToken

        // Create habit
        val createHabitRequest = CreateHabitRequest(name = "Test Habit")
        val createResponse = mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createHabitRequest))
        ).andReturn()

        val habitResponse = objectMapper.readValue(
            createResponse.response.contentAsString,
            HabitResponse::class.java
        )
        val habitId = habitResponse.id

        // Test: Check habit for 5 consecutive days
        val today = LocalDate.now()
        for (i in 0..4) {
            val date = today.minusDays(i.toLong())
            val checkRequest = CheckHabitRequest(date = date)
            mockMvc.perform(
                post("/habits/$habitId/check")
                    .header("Authorization", "Bearer $jwtToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(checkRequest))
            )
        }

        // Verify streak is 5
        val statsResponse = mockMvc.perform(
            get("/stats")
                .header("Authorization", "Bearer $jwtToken")
        ).andReturn()

        val stats = objectMapper.readValue(
            statsResponse.response.contentAsString,
            StatsResponse::class.java
        )
        
        assertEquals(5, stats.habits[0].currentStreak)
        assertEquals(5, stats.habits[0].totalCompletions)

        // Test: Add gap and verify streak breaks
        val gapDate = today.minusDays(7)
        val checkGapRequest = CheckHabitRequest(date = gapDate)
        mockMvc.perform(
            post("/habits/$habitId/check")
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkGapRequest))
        )

        // Streak should still be 5 (gap at day 7)
        val statsAfterGap = mockMvc.perform(
            get("/stats")
                .header("Authorization", "Bearer $jwtToken")
        ).andReturn()

        val statsGap = objectMapper.readValue(
            statsAfterGap.response.contentAsString,
            StatsResponse::class.java
        )
        
        assertEquals(5, statsGap.habits[0].currentStreak)
        assertEquals(6, statsGap.habits[0].totalCompletions)
    }
}
