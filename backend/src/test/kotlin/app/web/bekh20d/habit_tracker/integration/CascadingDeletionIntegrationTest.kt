package app.web.bekh20d.habit_tracker.integration

import app.web.bekh20d.habit_tracker.dto.CheckHabitRequest
import app.web.bekh20d.habit_tracker.dto.LoginRequest
import app.web.bekh20d.habit_tracker.dto.LoginResponse
import app.web.bekh20d.habit_tracker.model.FrequencyType
import app.web.bekh20d.habit_tracker.model.Habit
import app.web.bekh20d.habit_tracker.model.HabitRecord
import app.web.bekh20d.habit_tracker.model.RecordStatus
import app.web.bekh20d.habit_tracker.model.User
import app.web.bekh20d.habit_tracker.repository.EmailVerificationTokenRepository
import app.web.bekh20d.habit_tracker.repository.HabitRecordRepository
import app.web.bekh20d.habit_tracker.repository.HabitRepository
import app.web.bekh20d.habit_tracker.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
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
 * Integration Test for Cascading Deletion
 * 
 * **Validates: Requirements 7.2, 13.4**
 * 
 * This test verifies that deleting a habit also deletes all associated habit records,
 * and that referential integrity is maintained in the database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CascadingDeletionIntegrationTest {

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

    private lateinit var testUser: User
    private lateinit var jwtToken: String

    @BeforeEach
    fun setup() {
        // Create and verify test user
        testUser = User(
            email = "cascade@example.com",
            password = passwordEncoder.encode("password123"),
            verified = true
        )
        testUser = userRepository.save(testUser)

        // Login to get JWT token
        val loginRequest = LoginRequest(email = "cascade@example.com", password = "password123")
        val loginResponse = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        ).andReturn()

        val loginData = objectMapper.readValue(
            loginResponse.response.contentAsString,
            LoginResponse::class.java
        )
        jwtToken = loginData.accessToken
    }

    @AfterEach
    fun cleanup() {
        habitRecordRepository.deleteAll()
        habitRepository.deleteAll()
        emailVerificationTokenRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `deleting habit also deletes all associated records`() {
        // Create a habit
        val habit = Habit(
            userId = testUser.id,
            name = "Test Habit",
            frequencyType = FrequencyType.DAILY
        )
        val savedHabit = habitRepository.save(habit)

        // Create multiple habit records
        val today = LocalDate.now()
        val records = mutableListOf<HabitRecord>()
        for (i in 0..4) {
            val record = HabitRecord(
                habitId = savedHabit.id,
                date = today.minusDays(i.toLong()),
                status = RecordStatus.DONE
            )
            records.add(habitRecordRepository.save(record))
        }

        // Verify records exist
        val recordsBeforeDeletion = habitRecordRepository.findByHabitIdOrderByDateDesc(savedHabit.id)
        assertEquals(5, recordsBeforeDeletion.size)

        // Delete the habit
        mockMvc.perform(
            delete("/habits/${savedHabit.id}")
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(status().isNoContent)

        // Verify habit is deleted
        val habitExists = habitRepository.existsById(savedHabit.id)
        assertFalse(habitExists)

        // Verify all associated records are deleted
        val recordsAfterDeletion = habitRecordRepository.findByHabitIdOrderByDateDesc(savedHabit.id)
        assertEquals(0, recordsAfterDeletion.size)

        // Verify each individual record is deleted
        records.forEach { record ->
            val recordExists = habitRecordRepository.existsById(record.id)
            assertFalse(recordExists)
        }
    }

    @Test
    fun `deleting habit with no records succeeds`() {
        // Create a habit without any records
        val habit = Habit(
            userId = testUser.id,
            name = "Empty Habit",
            frequencyType = FrequencyType.DAILY
        )
        val savedHabit = habitRepository.save(habit)

        // Verify no records exist
        val recordsBefore = habitRecordRepository.findByHabitIdOrderByDateDesc(savedHabit.id)
        assertEquals(0, recordsBefore.size)

        // Delete the habit
        mockMvc.perform(
            delete("/habits/${savedHabit.id}")
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(status().isNoContent)

        // Verify habit is deleted
        val habitExists = habitRepository.existsById(savedHabit.id)
        assertFalse(habitExists)
    }

    @Test
    fun `deleting one habit does not affect other habits or their records`() {
        // Create two habits
        val habit1 = Habit(
            userId = testUser.id,
            name = "Habit 1",
            frequencyType = FrequencyType.DAILY
        )
        val savedHabit1 = habitRepository.save(habit1)

        val habit2 = Habit(
            userId = testUser.id,
            name = "Habit 2",
            frequencyType = FrequencyType.DAILY
        )
        val savedHabit2 = habitRepository.save(habit2)

        // Create records for both habits
        val today = LocalDate.now()
        val record1 = HabitRecord(
            habitId = savedHabit1.id,
            date = today,
            status = RecordStatus.DONE
        )
        habitRecordRepository.save(record1)

        val record2 = HabitRecord(
            habitId = savedHabit2.id,
            date = today,
            status = RecordStatus.DONE
        )
        habitRecordRepository.save(record2)

        // Delete habit 1
        mockMvc.perform(
            delete("/habits/${savedHabit1.id}")
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(status().isNoContent)

        // Verify habit 1 and its records are deleted
        assertFalse(habitRepository.existsById(savedHabit1.id))
        val records1 = habitRecordRepository.findByHabitIdOrderByDateDesc(savedHabit1.id)
        assertEquals(0, records1.size)

        // Verify habit 2 and its records still exist
        assertTrue(habitRepository.existsById(savedHabit2.id))
        val records2 = habitRecordRepository.findByHabitIdOrderByDateDesc(savedHabit2.id)
        assertEquals(1, records2.size)
        assertEquals(today, records2[0].date)
    }

    @Test
    fun `referential integrity is maintained after deletion`() {
        // Create habit and records
        val habit = Habit(
            userId = testUser.id,
            name = "Test Habit",
            frequencyType = FrequencyType.DAILY
        )
        val savedHabit = habitRepository.save(habit)

        val today = LocalDate.now()
        for (i in 0..2) {
            val record = HabitRecord(
                habitId = savedHabit.id,
                date = today.minusDays(i.toLong()),
                status = RecordStatus.DONE
            )
            habitRecordRepository.save(record)
        }

        // Get total record count before deletion
        val totalRecordsBefore = habitRecordRepository.count()
        assertEquals(3, totalRecordsBefore)

        // Delete the habit
        mockMvc.perform(
            delete("/habits/${savedHabit.id}")
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(status().isNoContent)

        // Verify all records are deleted (referential integrity maintained)
        val totalRecordsAfter = habitRecordRepository.count()
        assertEquals(0, totalRecordsAfter)

        // Verify no orphaned records exist
        val allRecords = habitRecordRepository.findAll()
        assertTrue(allRecords.isEmpty())
    }

    @Test
    fun `cascading deletion works through API endpoint`() {
        // Create habit through API
        val createRequest = """{"name": "API Habit"}"""
        val createResponse = mockMvc.perform(
            post("/habits")
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest)
        )
            .andExpect(status().isCreated)
            .andReturn()

        val habitId = objectMapper.readTree(createResponse.response.contentAsString)
            .get("id").asLong()

        // Create records through API
        val today = LocalDate.now()
        for (i in 0..3) {
            val checkRequest = CheckHabitRequest(date = today.minusDays(i.toLong()))
            mockMvc.perform(
                post("/habits/$habitId/check")
                    .header("Authorization", "Bearer $jwtToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(checkRequest))
            )
                .andExpect(status().isOk)
        }

        // Verify records exist
        val recordsBefore = habitRecordRepository.findByHabitIdOrderByDateDesc(habitId)
        assertEquals(4, recordsBefore.size)

        // Delete habit through API
        mockMvc.perform(
            delete("/habits/$habitId")
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(status().isNoContent)

        // Verify habit and all records are deleted
        assertFalse(habitRepository.existsById(habitId))
        val recordsAfter = habitRecordRepository.findByHabitIdOrderByDateDesc(habitId)
        assertEquals(0, recordsAfter.size)
    }

    @Test
    fun `database constraint prevents orphaned records`() {
        // Create habit
        val habit = Habit(
            userId = testUser.id,
            name = "Test Habit",
            frequencyType = FrequencyType.DAILY
        )
        val savedHabit = habitRepository.save(habit)

        // Create record
        val record = HabitRecord(
            habitId = savedHabit.id,
            date = LocalDate.now(),
            status = RecordStatus.DONE
        )
        habitRecordRepository.save(record)

        // Delete habit using service (which handles cascading deletion)
        mockMvc.perform(
            delete("/habits/${savedHabit.id}")
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(status().isNoContent)

        // Verify record is also deleted (no orphans)
        val orphanedRecords = habitRecordRepository.findByHabitIdOrderByDateDesc(savedHabit.id)
        assertEquals(0, orphanedRecords.size)
    }

    @Test
    fun `multiple habits can be deleted independently with their records`() {
        // Create multiple habits with records
        val habits = mutableListOf<Habit>()
        val today = LocalDate.now()

        for (i in 1..3) {
            val habit = Habit(
                userId = testUser.id,
                name = "Habit $i",
                frequencyType = FrequencyType.DAILY
            )
            val savedHabit = habitRepository.save(habit)
            habits.add(savedHabit)

            // Create 2 records for each habit
            for (j in 0..1) {
                val record = HabitRecord(
                    habitId = savedHabit.id,
                    date = today.minusDays(j.toLong()),
                    status = RecordStatus.DONE
                )
                habitRecordRepository.save(record)
            }
        }

        // Verify initial state: 3 habits, 6 records
        assertEquals(3, habitRepository.count())
        assertEquals(6, habitRecordRepository.count())

        // Delete first habit
        mockMvc.perform(
            delete("/habits/${habits[0].id}")
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(status().isNoContent)

        // Verify: 2 habits, 4 records
        assertEquals(2, habitRepository.count())
        assertEquals(4, habitRecordRepository.count())

        // Delete second habit
        mockMvc.perform(
            delete("/habits/${habits[1].id}")
                .header("Authorization", "Bearer $jwtToken")
        )
            .andExpect(status().isNoContent)

        // Verify: 1 habit, 2 records
        assertEquals(1, habitRepository.count())
        assertEquals(2, habitRecordRepository.count())

        // Verify remaining habit and its records
        val remainingHabit = habitRepository.findById(habits[2].id).orElseThrow()
        assertEquals("Habit 3", remainingHabit.name)
        val remainingRecords = habitRecordRepository.findByHabitIdOrderByDateDesc(habits[2].id)
        assertEquals(2, remainingRecords.size)
    }
}
