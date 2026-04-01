package app.web.bekh20d.habit_tracker.property

import app.web.bekh20d.habit_tracker.model.HabitRecord
import app.web.bekh20d.habit_tracker.model.RecordStatus
import app.web.bekh20d.habit_tracker.repository.HabitRecordRepository
import app.web.bekh20d.habit_tracker.repository.HabitRepository
import app.web.bekh20d.habit_tracker.service.StatsService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

/**
 * Property-Based Test for Dynamic Streak Calculation
 * 
 * **Property 13: Dynamic Streak Calculation**
 * **Validates: Requirements 10.4, 10.5**
 * 
 * This test verifies that:
 * - Streaks are calculated from records, not stored in the database
 * - Streak changes when new records are added
 * - Multiple calls to calculateStreak with same data return same result (deterministic)
 * - Adding a record changes the streak appropriately
 */
class DynamicStreakCalculationPropertyTest : StringSpec({

    "Property 13: streaks are calculated dynamically from records, not stored" {
        checkAll(
            10,
            Arb.long(1L..1000L),  // habit ID
            Arb.int(1..20)  // number of consecutive days
        ) { habitId, consecutiveDays ->
            // Arrange
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitRepository = mock<HabitRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            val today = LocalDate.now()
            val records = (0 until consecutiveDays).map { daysAgo ->
                HabitRecord(
                    id = daysAgo.toLong() + 1,
                    habitId = habitId,
                    date = today.minusDays(daysAgo.toLong()),
                    status = RecordStatus.DONE
                )
            }.sortedByDescending { it.date }
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(records)
            
            // Act: Calculate streak multiple times
            val streak1 = statsService.calculateStreak(habitId)
            val streak2 = statsService.calculateStreak(habitId)
            val streak3 = statsService.calculateStreak(habitId)
            
            // Assert: All calculations return the same result (deterministic)
            // This proves streaks are calculated from records, not stored state
            streak1 shouldBe consecutiveDays
            streak2 shouldBe consecutiveDays
            streak3 shouldBe consecutiveDays
            streak1 shouldBe streak2
            streak2 shouldBe streak3
        }
    }

    "Property 13: streak changes when new record is added to consecutive sequence" {
        checkAll(
            10,
            Arb.long(1L..1000L),  // habit ID
            Arb.int(1..15)  // initial consecutive days
        ) { habitId, initialDays ->
            // Arrange
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitRepository = mock<HabitRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            val today = LocalDate.now()
            
            // Initial records (consecutive from today)
            val initialRecords = (0 until initialDays).map { daysAgo ->
                HabitRecord(
                    id = daysAgo.toLong() + 1,
                    habitId = habitId,
                    date = today.minusDays(daysAgo.toLong()),
                    status = RecordStatus.DONE
                )
            }.sortedByDescending { it.date }
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(initialRecords)
            
            // Act: Calculate initial streak
            val initialStreak = statsService.calculateStreak(habitId)
            
            // Add a new record extending the streak backwards
            val newRecord = HabitRecord(
                id = (initialDays + 1).toLong(),
                habitId = habitId,
                date = today.minusDays(initialDays.toLong()),
                status = RecordStatus.DONE
            )
            val updatedRecords = (initialRecords + newRecord).sortedByDescending { it.date }
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(updatedRecords)
            
            // Act: Calculate streak after adding record
            val updatedStreak = statsService.calculateStreak(habitId)
            
            // Assert: Streak increased by 1 after adding consecutive record
            initialStreak shouldBe initialDays
            updatedStreak shouldBe (initialDays + 1)
            updatedStreak shouldBe (initialStreak + 1)
        }
    }

    "Property 13: streak changes when record is added that breaks the sequence" {
        checkAll(
            10,
            Arb.long(1L..1000L),  // habit ID
            Arb.int(2..10),  // consecutive days from today
            Arb.int(2..5)    // gap size
        ) { habitId, consecutiveDays, gapSize ->
            // Arrange
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitRepository = mock<HabitRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            val today = LocalDate.now()
            
            // Initial records (consecutive from today)
            val initialRecords = (0 until consecutiveDays).map { daysAgo ->
                HabitRecord(
                    id = daysAgo.toLong() + 1,
                    habitId = habitId,
                    date = today.minusDays(daysAgo.toLong()),
                    status = RecordStatus.DONE
                )
            }.sortedByDescending { it.date }
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(initialRecords)
            
            // Act: Calculate initial streak
            val initialStreak = statsService.calculateStreak(habitId)
            
            // Add a record after a gap (does not extend streak)
            val gappedRecord = HabitRecord(
                id = (consecutiveDays + 1).toLong(),
                habitId = habitId,
                date = today.minusDays((consecutiveDays + gapSize).toLong()),
                status = RecordStatus.DONE
            )
            val updatedRecords = (initialRecords + gappedRecord).sortedByDescending { it.date }
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(updatedRecords)
            
            // Act: Calculate streak after adding gapped record
            val updatedStreak = statsService.calculateStreak(habitId)
            
            // Assert: Streak remains the same (gapped record doesn't extend streak)
            initialStreak shouldBe consecutiveDays
            updatedStreak shouldBe consecutiveDays
            updatedStreak shouldBe initialStreak
        }
    }

    "Property 13: streak recalculates correctly when today's record is added" {
        checkAll(
            10,
            Arb.long(1L..1000L),  // habit ID
            Arb.int(1..10)  // consecutive days from yesterday
        ) { habitId, consecutiveDays ->
            // Arrange: Start with consecutive records from yesterday (today not done)
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitRepository = mock<HabitRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            
            // Initial records (consecutive from yesterday, today missing)
            val initialRecords = (0 until consecutiveDays).map { daysAgo ->
                HabitRecord(
                    id = daysAgo.toLong() + 1,
                    habitId = habitId,
                    date = yesterday.minusDays(daysAgo.toLong()),
                    status = RecordStatus.DONE
                )
            }.sortedByDescending { it.date }
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(initialRecords)
            
            // Act: Calculate initial streak (should count from yesterday)
            val initialStreak = statsService.calculateStreak(habitId)
            
            // Add today's record
            val todayRecord = HabitRecord(
                id = (consecutiveDays + 1).toLong(),
                habitId = habitId,
                date = today,
                status = RecordStatus.DONE
            )
            val updatedRecords = (listOf(todayRecord) + initialRecords).sortedByDescending { it.date }
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(updatedRecords)
            
            // Act: Calculate streak after adding today's record
            val updatedStreak = statsService.calculateStreak(habitId)
            
            // Assert: Streak increased by 1 when today's record is added
            initialStreak shouldBe consecutiveDays
            updatedStreak shouldBe (consecutiveDays + 1)
            updatedStreak shouldBe (initialStreak + 1)
        }
    }

    "Property 13: streak is recalculated on every call, not cached" {
        checkAll(
            10,
            Arb.long(1L..1000L),  // habit ID
            Arb.int(1..10)  // initial consecutive days
        ) { habitId, initialDays ->
            // Arrange
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitRepository = mock<HabitRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            val today = LocalDate.now()
            
            // Initial records
            val initialRecords = (0 until initialDays).map { daysAgo ->
                HabitRecord(
                    id = daysAgo.toLong() + 1,
                    habitId = habitId,
                    date = today.minusDays(daysAgo.toLong()),
                    status = RecordStatus.DONE
                )
            }.sortedByDescending { it.date }
            
            // First call: return initial records
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(initialRecords)
            
            val streak1 = statsService.calculateStreak(habitId)
            
            // Simulate adding a new record by changing mock behavior
            val newRecord = HabitRecord(
                id = (initialDays + 1).toLong(),
                habitId = habitId,
                date = today.minusDays(initialDays.toLong()),
                status = RecordStatus.DONE
            )
            val updatedRecords = (initialRecords + newRecord).sortedByDescending { it.date }
            
            // Second call: return updated records
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(updatedRecords)
            
            val streak2 = statsService.calculateStreak(habitId)
            
            // Assert: Streak changed because it's recalculated, not cached
            streak1 shouldBe initialDays
            streak2 shouldBe (initialDays + 1)
            streak2 shouldNotBe streak1
        }
    }

    "Property 13: removing records decreases streak dynamically" {
        checkAll(
            10,
            Arb.long(1L..1000L),  // habit ID
            Arb.int(3..15)  // initial consecutive days (at least 3)
        ) { habitId, initialDays ->
            // Arrange
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitRepository = mock<HabitRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            val today = LocalDate.now()
            
            // Initial records (consecutive from today)
            val initialRecords = (0 until initialDays).map { daysAgo ->
                HabitRecord(
                    id = daysAgo.toLong() + 1,
                    habitId = habitId,
                    date = today.minusDays(daysAgo.toLong()),
                    status = RecordStatus.DONE
                )
            }.sortedByDescending { it.date }
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(initialRecords)
            
            // Act: Calculate initial streak
            val initialStreak = statsService.calculateStreak(habitId)
            
            // Remove today's record (simulate deletion)
            val recordsWithoutToday = initialRecords.drop(1)
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(recordsWithoutToday)
            
            // Act: Calculate streak after removing today's record
            val updatedStreak = statsService.calculateStreak(habitId)
            
            // Assert: Streak decreased by 1 after removing today's record
            initialStreak shouldBe initialDays
            updatedStreak shouldBe (initialDays - 1)
            updatedStreak shouldBe (initialStreak - 1)
        }
    }

    "Property 13: empty records always return zero streak (no stored state)" {
        checkAll(
            5,
            Arb.long(1L..1000L)  // habit ID
        ) { habitId ->
            // Arrange
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitRepository = mock<HabitRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            // Mock empty records
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(emptyList())
            
            // Act: Calculate streak multiple times
            val streak1 = statsService.calculateStreak(habitId)
            val streak2 = statsService.calculateStreak(habitId)
            val streak3 = statsService.calculateStreak(habitId)
            
            // Assert: Always returns 0 (no stored state, purely calculated)
            streak1 shouldBe 0
            streak2 shouldBe 0
            streak3 shouldBe 0
        }
    }
})
