package app.web.bekh20d.habit_tracker.property

import app.web.bekh20d.habit_tracker.model.HabitRecord
import app.web.bekh20d.habit_tracker.model.RecordStatus
import app.web.bekh20d.habit_tracker.repository.HabitRecordRepository
import app.web.bekh20d.habit_tracker.repository.HabitRepository
import app.web.bekh20d.habit_tracker.service.StatsService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

/**
 * Property-Based Test for Streak Calculation Correctness
 * 
 * **Property 5: Streak Calculation Correctness**
 * **Validates: Requirements 9.1, 9.2, 9.3, 9.6, 9.7**
 * 
 * This test verifies that:
 * - Streak is always non-negative
 * - Streak never exceeds total records
 * - Streak represents consecutive days from today or yesterday
 * - Gaps in dates break the streak
 */
class StreakCalculationCorrectnessPropertyTest : StringSpec({

    "Property 5: streak is always non-negative" {
        checkAll(
            10,
            Arb.long(1L..1000L),  // habit ID
            Arb.list(Arb.localDate(LocalDate.now().minusDays(365), LocalDate.now().plusDays(30)), 0..50)
        ) { habitId, dates ->
            // Arrange
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitRepository = mock<HabitRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            val records = dates.mapIndexed { index, date ->
                HabitRecord(
                    id = index.toLong() + 1,
                    habitId = habitId,
                    date = date,
                    status = RecordStatus.DONE
                )
            }.sortedByDescending { it.date }
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(records)
            
            // Act
            val streak = statsService.calculateStreak(habitId)
            
            // Assert: Streak is always >= 0
            streak shouldBeGreaterThanOrEqual 0
        }
    }

    "Property 5: streak never exceeds total number of records" {
        checkAll(
            10,
            Arb.long(1L..1000L),  // habit ID
            Arb.list(Arb.localDate(LocalDate.now().minusDays(365), LocalDate.now().plusDays(30)), 1..50)
        ) { habitId, dates ->
            // Arrange
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitRepository = mock<HabitRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            val records = dates.mapIndexed { index, date ->
                HabitRecord(
                    id = index.toLong() + 1,
                    habitId = habitId,
                    date = date,
                    status = RecordStatus.DONE
                )
            }.sortedByDescending { it.date }
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(records)
            
            // Act
            val streak = statsService.calculateStreak(habitId)
            
            // Assert: Streak <= total records
            streak shouldBeLessThanOrEqual records.size
        }
    }

    "Property 5: streak with consecutive days from today" {
        checkAll(
            10,
            Arb.long(1L..1000L),  // habit ID
            Arb.int(1..30)  // number of consecutive days
        ) { habitId, consecutiveDays ->
            // Arrange: Create consecutive records from today backwards
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
            
            // Act
            val streak = statsService.calculateStreak(habitId)
            
            // Assert: Streak equals number of consecutive days
            streak shouldBe consecutiveDays
        }
    }

    "Property 5: streak with consecutive days from yesterday (today not done)" {
        checkAll(
            10,
            Arb.long(1L..1000L),  // habit ID
            Arb.int(1..30)  // number of consecutive days
        ) { habitId, consecutiveDays ->
            // Arrange: Create consecutive records from yesterday backwards (skip today)
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitRepository = mock<HabitRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            val yesterday = LocalDate.now().minusDays(1)
            val records = (0 until consecutiveDays).map { daysAgo ->
                HabitRecord(
                    id = daysAgo.toLong() + 1,
                    habitId = habitId,
                    date = yesterday.minusDays(daysAgo.toLong()),
                    status = RecordStatus.DONE
                )
            }.sortedByDescending { it.date }
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(records)
            
            // Act
            val streak = statsService.calculateStreak(habitId)
            
            // Assert: Streak equals number of consecutive days from yesterday
            streak shouldBe consecutiveDays
        }
    }

    "Property 5: gaps in dates break the streak" {
        checkAll(
            10,
            Arb.long(1L..1000L),  // habit ID
            Arb.int(1..10),  // consecutive days before gap
            Arb.int(2..10)   // gap size in days
        ) { habitId, consecutiveDays, gapSize ->
            // Arrange: Create consecutive records from today, then a gap, then more records
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitRepository = mock<HabitRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            val today = LocalDate.now()
            
            // Recent consecutive records (from today backwards)
            val recentRecords = (0 until consecutiveDays).map { daysAgo ->
                HabitRecord(
                    id = daysAgo.toLong() + 1,
                    habitId = habitId,
                    date = today.minusDays(daysAgo.toLong()),
                    status = RecordStatus.DONE
                )
            }
            
            // Older records after the gap
            val olderRecords = (0..5).map { daysAgo ->
                HabitRecord(
                    id = (consecutiveDays + daysAgo).toLong() + 1,
                    habitId = habitId,
                    date = today.minusDays((consecutiveDays + gapSize + daysAgo).toLong()),
                    status = RecordStatus.DONE
                )
            }
            
            val allRecords = (recentRecords + olderRecords).sortedByDescending { it.date }
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(allRecords)
            
            // Act
            val streak = statsService.calculateStreak(habitId)
            
            // Assert: Streak only counts consecutive days before the gap
            streak shouldBe consecutiveDays
        }
    }

    "Property 5: empty records return zero streak" {
        checkAll(
            5,
            Arb.long(1L..1000L)  // habit ID
        ) { habitId ->
            // Arrange
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitRepository = mock<HabitRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(emptyList())
            
            // Act
            val streak = statsService.calculateStreak(habitId)
            
            // Assert: Empty records return 0 streak
            streak shouldBe 0
        }
    }

    "Property 5: future dates do not contribute to streak" {
        checkAll(
            10,
            Arb.long(1L..1000L),  // habit ID
            Arb.int(1..10),  // consecutive days from today
            Arb.int(1..5)    // future days
        ) { habitId, consecutiveDays, futureDays ->
            // Arrange: Create consecutive records from today + some future records
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitRepository = mock<HabitRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            val today = LocalDate.now()
            
            // Current/past consecutive records
            val currentRecords = (0 until consecutiveDays).map { daysAgo ->
                HabitRecord(
                    id = daysAgo.toLong() + 1,
                    habitId = habitId,
                    date = today.minusDays(daysAgo.toLong()),
                    status = RecordStatus.DONE
                )
            }
            
            // Future records (should be ignored)
            val futureRecords = (1..futureDays).map { daysAhead ->
                HabitRecord(
                    id = (consecutiveDays + daysAhead).toLong(),
                    habitId = habitId,
                    date = today.plusDays(daysAhead.toLong()),
                    status = RecordStatus.DONE
                )
            }
            
            val allRecords = (currentRecords + futureRecords).sortedByDescending { it.date }
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(allRecords)
            
            // Act
            val streak = statsService.calculateStreak(habitId)
            
            // Assert: Streak only counts current/past consecutive days
            streak shouldBe consecutiveDays
        }
    }

    "Property 5: single record today gives streak of 1" {
        checkAll(
            5,
            Arb.long(1L..1000L)  // habit ID
        ) { habitId ->
            // Arrange
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitRepository = mock<HabitRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            val today = LocalDate.now()
            val records = listOf(
                HabitRecord(
                    id = 1L,
                    habitId = habitId,
                    date = today,
                    status = RecordStatus.DONE
                )
            )
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(records)
            
            // Act
            val streak = statsService.calculateStreak(habitId)
            
            // Assert: Single record today gives streak of 1
            streak shouldBe 1
        }
    }

    "Property 5: single record yesterday gives streak of 1" {
        checkAll(
            5,
            Arb.long(1L..1000L)  // habit ID
        ) { habitId ->
            // Arrange
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitRepository = mock<HabitRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            val yesterday = LocalDate.now().minusDays(1)
            val records = listOf(
                HabitRecord(
                    id = 1L,
                    habitId = habitId,
                    date = yesterday,
                    status = RecordStatus.DONE
                )
            )
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(records)
            
            // Act
            val streak = statsService.calculateStreak(habitId)
            
            // Assert: Single record yesterday gives streak of 1
            streak shouldBe 1
        }
    }

    "Property 5: record older than yesterday gives streak of 0" {
        checkAll(
            5,
            Arb.long(1L..1000L),  // habit ID
            Arb.int(2..30)  // days ago (at least 2)
        ) { habitId, daysAgo ->
            // Arrange
            val habitRecordRepository = mock<HabitRecordRepository>()
            val habitRepository = mock<HabitRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            val oldDate = LocalDate.now().minusDays(daysAgo.toLong())
            val records = listOf(
                HabitRecord(
                    id = 1L,
                    habitId = habitId,
                    date = oldDate,
                    status = RecordStatus.DONE
                )
            )
            
            whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(records)
            
            // Act
            val streak = statsService.calculateStreak(habitId)
            
            // Assert: Record older than yesterday gives streak of 0 (gap exists)
            streak shouldBe 0
        }
    }
})
