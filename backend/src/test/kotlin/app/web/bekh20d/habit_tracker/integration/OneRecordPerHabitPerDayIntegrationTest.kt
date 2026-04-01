package app.web.bekh20d.habit_tracker.integration

import app.web.bekh20d.habit_tracker.model.FrequencyType
import app.web.bekh20d.habit_tracker.model.Habit
import app.web.bekh20d.habit_tracker.model.HabitRecord
import app.web.bekh20d.habit_tracker.model.RecordStatus
import app.web.bekh20d.habit_tracker.model.User
import app.web.bekh20d.habit_tracker.repository.HabitRecordRepository
import app.web.bekh20d.habit_tracker.repository.HabitRepository
import app.web.bekh20d.habit_tracker.repository.UserRepository
import app.web.bekh20d.habit_tracker.service.HabitService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

/**
 * Integration Test for One Record Per Habit Per Day Database Constraint
 * 
 * **Validates: Requirements 8.2, 8.6, 13.2**
 * 
 * This test verifies that the database constraint UNIQUE(habit_id, date) prevents
 * duplicate records from being created at the database level.
 */
@SpringBootTest
@ActiveProfiles("test")
class OneRecordPerHabitPerDayIntegrationTest {

    @Autowired
    private lateinit var habitService: HabitService

    @Autowired
    private lateinit var habitRepository: HabitRepository

    @Autowired
    private lateinit var habitRecordRepository: HabitRecordRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var testUser: User
    private lateinit var testHabit: Habit

    @BeforeEach
    fun setup() {
        // Create test user
        testUser = User(
            email = "test@example.com",
            password = "hashedPassword",
            verified = true
        )
        testUser = userRepository.save(testUser)

        // Create test habit
        testHabit = Habit(
            userId = testUser.id,
            name = "Test Habit",
            frequencyType = FrequencyType.DAILY
        )
        testHabit = habitRepository.save(testHabit)
    }

    @AfterEach
    fun cleanup() {
        habitRecordRepository.deleteAll()
        habitRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `database constraint prevents duplicate records for same habit and date`() {
        // Arrange
        val date = LocalDate.of(2024, 1, 15)

        // Act: Create first record directly in repository
        val firstRecord = HabitRecord(
            habitId = testHabit.id,
            date = date,
            status = RecordStatus.DONE
        )
        habitRecordRepository.save(firstRecord)
        habitRecordRepository.flush()

        // Assert: Attempting to create duplicate record throws DataIntegrityViolationException
        assertThrows(DataIntegrityViolationException::class.java) {
            val duplicateRecord = HabitRecord(
                habitId = testHabit.id,
                date = date,
                status = RecordStatus.DONE
            )
            habitRecordRepository.save(duplicateRecord)
            habitRecordRepository.flush()
        }
    }

    @Test
    fun `service upsert logic prevents duplicate records`() {
        // Arrange
        val date = LocalDate.of(2024, 1, 15)

        // Act: Check habit twice on same date using service
        val firstCheck = habitService.checkHabit(testHabit.id, testUser.id, date)
        val secondCheck = habitService.checkHabit(testHabit.id, testUser.id, date)

        // Assert: Both checks succeed and return same record ID
        assertEquals(firstCheck.id, secondCheck.id)
        assertEquals(firstCheck.habitId, secondCheck.habitId)
        assertEquals(firstCheck.date, secondCheck.date)

        // Verify only one record exists in database
        val records = habitRecordRepository.findByHabitIdAndDate(testHabit.id, date)
        assertEquals(firstCheck.id, records?.id)
    }

    @Test
    fun `only one record exists per habit per date after multiple checks`() {
        // Arrange
        val date = LocalDate.of(2024, 1, 15)

        // Act: Check habit multiple times
        habitService.checkHabit(testHabit.id, testUser.id, date)
        habitService.checkHabit(testHabit.id, testUser.id, date)
        habitService.checkHabit(testHabit.id, testUser.id, date)

        // Assert: Only one record exists
        val allRecords = habitRecordRepository.findByHabitIdOrderByDateDesc(testHabit.id)
        assertEquals(1, allRecords.size)
        assertEquals(date, allRecords[0].date)
    }

    @Test
    fun `different dates create separate records for same habit`() {
        // Arrange
        val date1 = LocalDate.of(2024, 1, 15)
        val date2 = LocalDate.of(2024, 1, 16)
        val date3 = LocalDate.of(2024, 1, 17)

        // Act: Check habit on different dates
        habitService.checkHabit(testHabit.id, testUser.id, date1)
        habitService.checkHabit(testHabit.id, testUser.id, date2)
        habitService.checkHabit(testHabit.id, testUser.id, date3)

        // Assert: Three separate records exist
        val allRecords = habitRecordRepository.findByHabitIdOrderByDateDesc(testHabit.id)
        assertEquals(3, allRecords.size)
    }

    @Test
    fun `different habits can have records on same date`() {
        // Arrange
        val habit2 = Habit(
            userId = testUser.id,
            name = "Test Habit 2",
            frequencyType = FrequencyType.DAILY
        )
        val savedHabit2 = habitRepository.save(habit2)
        val date = LocalDate.of(2024, 1, 15)

        // Act: Check both habits on same date
        habitService.checkHabit(testHabit.id, testUser.id, date)
        habitService.checkHabit(savedHabit2.id, testUser.id, date)

        // Assert: Two separate records exist
        val records1 = habitRecordRepository.findByHabitIdAndDate(testHabit.id, date)
        val records2 = habitRecordRepository.findByHabitIdAndDate(savedHabit2.id, date)
        
        assertEquals(testHabit.id, records1?.habitId)
        assertEquals(savedHabit2.id, records2?.habitId)
        assertEquals(date, records1?.date)
        assertEquals(date, records2?.date)
    }
}
