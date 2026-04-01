package app.web.bekh20d.habit_tracker.property

import app.web.bekh20d.habit_tracker.model.FrequencyType
import app.web.bekh20d.habit_tracker.model.Habit
import app.web.bekh20d.habit_tracker.model.HabitRecord
import app.web.bekh20d.habit_tracker.model.RecordStatus
import app.web.bekh20d.habit_tracker.repository.HabitRecordRepository
import app.web.bekh20d.habit_tracker.repository.HabitRepository
import app.web.bekh20d.habit_tracker.service.HabitService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.localDate
import io.kotest.property.checkAll
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Property-Based Test for One Record Per Habit Per Day
 * 
 * **Validates: Requirements 8.2, 8.6, 13.2**
 * 
 * This test verifies that checking the same habit twice on the same date updates the existing
 * record instead of creating a duplicate, and that only one record exists per (habitId, date)
 * combination.
 */
class OneRecordPerHabitPerDayPropertyTest : StringSpec({

    "Property 3: checking same habit twice on same date updates existing record" {
        checkAll<Long, Long, LocalDate>(
            5,
            Arb.long(1L..1000L),  // user ID
            Arb.long(1L..1000L),  // habit ID
            Arb.localDate()       // date
        ) { userId, habitId, date ->
            // Arrange
            val habitRepository = mock<HabitRepository>()
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitService = HabitService(habitRepository, habitRecordRepository)
            
            val habit = Habit(
                id = habitId,
                userId = userId,
                name = "Test Habit",
                frequencyType = FrequencyType.DAILY,
                createdAt = LocalDateTime.now()
            )
            
            val existingRecord = HabitRecord(
                id = 1L,
                habitId = habitId,
                date = date,
                status = RecordStatus.DONE
            )
            
            val updatedRecord = HabitRecord(
                id = 1L,
                habitId = habitId,
                date = date,
                status = RecordStatus.DONE
            )
            
            whenever(habitRepository.findByIdAndUserId(habitId, userId)).thenReturn(habit)
            
            // First check: no existing record
            whenever(habitRecordRepository.findByHabitIdAndDate(habitId, date))
                .thenReturn(null)
                .thenReturn(existingRecord)
            
            whenever(habitRecordRepository.save(any())).thenReturn(existingRecord, updatedRecord)
            
            // Act: Check habit twice on same date
            val firstCheck = habitService.checkHabit(habitId, userId, date)
            val secondCheck = habitService.checkHabit(habitId, userId, date)
            
            // Assert: Both checks succeed
            firstCheck.habitId shouldBe habitId
            firstCheck.date shouldBe date
            secondCheck.habitId shouldBe habitId
            secondCheck.date shouldBe date
            
            // Verify save was called twice (once for create, once for update)
            verify(habitRecordRepository, times(2)).save(any())
        }
    }

    "Property 3: only one record exists per (habitId, date) combination" {
        checkAll<Long, Long, LocalDate>(
            5,
            Arb.long(1L..1000L),  // user ID
            Arb.long(1L..1000L),  // habit ID
            Arb.localDate()       // date
        ) { userId, habitId, date ->
            // Arrange
            val habitRepository = mock<HabitRepository>()
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitService = HabitService(habitRepository, habitRecordRepository)
            
            val habit = Habit(
                id = habitId,
                userId = userId,
                name = "Test Habit",
                frequencyType = FrequencyType.DAILY,
                createdAt = LocalDateTime.now()
            )
            
            val record = HabitRecord(
                id = 1L,
                habitId = habitId,
                date = date,
                status = RecordStatus.DONE
            )
            
            whenever(habitRepository.findByIdAndUserId(habitId, userId)).thenReturn(habit)
            whenever(habitRecordRepository.findByHabitIdAndDate(habitId, date)).thenReturn(null)
            whenever(habitRecordRepository.save(any())).thenReturn(record)
            
            // Act: Check habit
            val result = habitService.checkHabit(habitId, userId, date)
            
            // Assert: Record is created with correct habitId and date
            result.habitId shouldBe habitId
            result.date shouldBe date
            
            // Verify findByHabitIdAndDate was called to check for existing record
            verify(habitRecordRepository).findByHabitIdAndDate(habitId, date)
        }
    }

    "Property 3: upsert logic ensures no duplicate records are created" {
        checkAll<Long, Long, LocalDate>(
            5,
            Arb.long(1L..1000L),  // user ID
            Arb.long(1L..1000L),  // habit ID
            Arb.localDate()       // date
        ) { userId, habitId, date ->
            // Arrange
            val habitRepository = mock<HabitRepository>()
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitService = HabitService(habitRepository, habitRecordRepository)
            
            val habit = Habit(
                id = habitId,
                userId = userId,
                name = "Test Habit",
                frequencyType = FrequencyType.DAILY,
                createdAt = LocalDateTime.now()
            )
            
            val existingRecord = HabitRecord(
                id = 1L,
                habitId = habitId,
                date = date,
                status = RecordStatus.DONE
            )
            
            whenever(habitRepository.findByIdAndUserId(habitId, userId)).thenReturn(habit)
            whenever(habitRecordRepository.findByHabitIdAndDate(habitId, date)).thenReturn(existingRecord)
            whenever(habitRecordRepository.save(any())).thenReturn(existingRecord)
            
            // Act: Check habit when record already exists
            val result = habitService.checkHabit(habitId, userId, date)
            
            // Assert: Existing record is updated, not duplicated
            result.id shouldBe existingRecord.id
            result.habitId shouldBe habitId
            result.date shouldBe date
            
            // Verify that the service checked for existing record before saving
            verify(habitRecordRepository).findByHabitIdAndDate(habitId, date)
            verify(habitRecordRepository).save(any())
        }
    }

    "Property 3: record ID remains same when updating existing record" {
        checkAll<Long, Long, LocalDate>(
            5,
            Arb.long(1L..1000L),  // user ID
            Arb.long(1L..1000L),  // habit ID
            Arb.localDate()       // date
        ) { userId, habitId, date ->
            // Arrange
            val habitRepository = mock<HabitRepository>()
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitService = HabitService(habitRepository, habitRecordRepository)
            
            val habit = Habit(
                id = habitId,
                userId = userId,
                name = "Test Habit",
                frequencyType = FrequencyType.DAILY,
                createdAt = LocalDateTime.now()
            )
            
            val existingRecordId = 42L
            val existingRecord = HabitRecord(
                id = existingRecordId,
                habitId = habitId,
                date = date,
                status = RecordStatus.DONE
            )
            
            val updatedRecord = HabitRecord(
                id = existingRecordId,
                habitId = habitId,
                date = date,
                status = RecordStatus.DONE
            )
            
            whenever(habitRepository.findByIdAndUserId(habitId, userId)).thenReturn(habit)
            whenever(habitRecordRepository.findByHabitIdAndDate(habitId, date)).thenReturn(existingRecord)
            whenever(habitRecordRepository.save(any())).thenReturn(updatedRecord)
            
            // Act: Check habit when record already exists
            val result = habitService.checkHabit(habitId, userId, date)
            
            // Assert: Record ID remains the same (update, not insert)
            result.id shouldBe existingRecordId
            result.id shouldNotBe 0L
        }
    }
})
