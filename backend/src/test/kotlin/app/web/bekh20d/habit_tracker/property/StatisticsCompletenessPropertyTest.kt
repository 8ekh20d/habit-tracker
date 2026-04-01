package app.web.bekh20d.habit_tracker.property

import app.web.bekh20d.habit_tracker.model.FrequencyType
import app.web.bekh20d.habit_tracker.model.Habit
import app.web.bekh20d.habit_tracker.model.HabitRecord
import app.web.bekh20d.habit_tracker.model.RecordStatus
import app.web.bekh20d.habit_tracker.repository.HabitRecordRepository
import app.web.bekh20d.habit_tracker.repository.HabitRepository
import app.web.bekh20d.habit_tracker.service.StatsService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.filter
import io.kotest.property.checkAll
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.time.LocalDate

/**
 * Property-Based Test for Statistics Completeness
 * 
 * **Property 12: Statistics Completeness**
 * **Validates: Requirements 10.1, 5.1**
 * 
 * This test verifies that statistics include all user habits and do not include
 * other users' habits. It uses property-based testing to generate random user IDs
 * and habit data to ensure the completeness invariant holds across all possible inputs.
 */
class StatisticsCompletenessPropertyTest : StringSpec({

    "Property 12: stats include all user habits" {
        checkAll<Long, List<String>>(
            5,
            Arb.long(1L..1000L),  // user ID
            Arb.list(Arb.string(1..100).filter { it.isNotBlank() }, 1..10)  // list of habit names
        ) { userId, habitNames ->
            // Arrange
            val habitRepository = mock<HabitRepository>()
            val habitRecordRepository = mock<HabitRecordRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            // Create habits for the user
            val userHabits = habitNames.mapIndexed { index, name ->
                Habit(
                    id = index.toLong() + 1,
                    userId = userId,
                    name = name,
                    frequencyType = FrequencyType.DAILY,
                    createdAt = LocalDateTime.now()
                )
            }
            
            // Mock repository to return user's habits
            whenever(habitRepository.findByUserId(userId)).thenReturn(userHabits)
            
            // Mock empty records for simplicity (we're testing completeness, not streak calculation)
            userHabits.forEach { habit ->
                whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habit.id)).thenReturn(emptyList())
            }
            
            // Act
            val stats = statsService.calculateStats(userId)
            
            // Assert: Stats include all user habits
            stats.habits.size shouldBe userHabits.size
            
            // Verify all habit IDs are present
            val statsHabitIds = stats.habits.map { it.habitId }
            val expectedHabitIds = userHabits.map { it.id }
            statsHabitIds shouldContainExactlyInAnyOrder expectedHabitIds
            
            // Verify all habit names are present
            val statsHabitNames = stats.habits.map { it.habitName }
            val expectedHabitNames = userHabits.map { it.name }
            statsHabitNames shouldContainExactlyInAnyOrder expectedHabitNames
        }
    }

    "Property 12: stats do not include other users' habits" {
        checkAll<Long, Long, List<String>, List<String>>(
            5,
            Arb.long(1L..1000L),  // user ID
            Arb.long(1L..1000L),  // other user ID
            Arb.list(Arb.string(1..100).filter { it.isNotBlank() }, 1..5),  // user's habit names
            Arb.list(Arb.string(1..100).filter { it.isNotBlank() }, 1..5)   // other user's habit names
        ) { userId, otherUserId, userHabitNames, otherUserHabitNames ->
            // Skip if user IDs are the same
            if (userId == otherUserId) return@checkAll
            
            // Arrange
            val habitRepository = mock<HabitRepository>()
            val habitRecordRepository = mock<HabitRecordRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            // Create habits for the user
            val userHabits = userHabitNames.mapIndexed { index, name ->
                Habit(
                    id = index.toLong() + 1,
                    userId = userId,
                    name = name,
                    frequencyType = FrequencyType.DAILY,
                    createdAt = LocalDateTime.now()
                )
            }
            
            // Create habits for the other user
            val otherUserHabits = otherUserHabitNames.mapIndexed { index, name ->
                Habit(
                    id = (userHabits.size + index + 1).toLong(),
                    userId = otherUserId,
                    name = "Other: $name",  // Prefix to ensure uniqueness
                    frequencyType = FrequencyType.DAILY,
                    createdAt = LocalDateTime.now()
                )
            }
            
            // Mock repository to return only user's habits
            whenever(habitRepository.findByUserId(userId)).thenReturn(userHabits)
            whenever(habitRepository.findByUserId(otherUserId)).thenReturn(otherUserHabits)
            
            // Mock empty records for all habits
            (userHabits + otherUserHabits).forEach { habit ->
                whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habit.id)).thenReturn(emptyList())
            }
            
            // Act
            val userStats = statsService.calculateStats(userId)
            val otherUserStats = statsService.calculateStats(otherUserId)
            
            // Assert: User stats do not include other user's habits
            val userStatsHabitIds = userStats.habits.map { it.habitId }
            val otherUserHabitIds = otherUserHabits.map { it.id }
            
            otherUserHabitIds.forEach { otherHabitId ->
                userStatsHabitIds shouldNotContain otherHabitId
            }
            
            // Assert: Other user stats do not include user's habits
            val otherUserStatsHabitIds = otherUserStats.habits.map { it.habitId }
            val userHabitIds = userHabits.map { it.id }
            
            userHabitIds.forEach { userHabitId ->
                otherUserStatsHabitIds shouldNotContain userHabitId
            }
            
            // Assert: Each user sees only their own habits
            userStats.habits.size shouldBe userHabits.size
            otherUserStats.habits.size shouldBe otherUserHabits.size
        }
    }

    "Property 12: stats completeness with habit records" {
        checkAll<Long, List<String>>(
            5,
            Arb.long(1L..1000L),  // user ID
            Arb.list(Arb.string(1..100).filter { it.isNotBlank() }, 1..5)  // habit names
        ) { userId, habitNames ->
            // Arrange
            val habitRepository = mock<HabitRepository>()
            val habitRecordRepository = mock<HabitRecordRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            // Create habits for the user
            val userHabits = habitNames.mapIndexed { index, name ->
                Habit(
                    id = index.toLong() + 1,
                    userId = userId,
                    name = name,
                    frequencyType = FrequencyType.DAILY,
                    createdAt = LocalDateTime.now()
                )
            }
            
            // Mock repository to return user's habits
            whenever(habitRepository.findByUserId(userId)).thenReturn(userHabits)
            
            // Create some records for each habit
            userHabits.forEach { habit ->
                val records = listOf(
                    HabitRecord(
                        id = habit.id * 100,
                        habitId = habit.id,
                        date = LocalDate.now(),
                        status = RecordStatus.DONE
                    ),
                    HabitRecord(
                        id = habit.id * 100 + 1,
                        habitId = habit.id,
                        date = LocalDate.now().minusDays(1),
                        status = RecordStatus.DONE
                    )
                )
                whenever(habitRecordRepository.findByHabitIdOrderByDateDesc(habit.id)).thenReturn(records)
            }
            
            // Act
            val stats = statsService.calculateStats(userId)
            
            // Assert: Stats include all user habits even when they have records
            stats.habits.size shouldBe userHabits.size
            
            // Verify all habits are present
            val statsHabitIds = stats.habits.map { it.habitId }
            val expectedHabitIds = userHabits.map { it.id }
            statsHabitIds shouldContainExactlyInAnyOrder expectedHabitIds
            
            // Verify each habit has statistics calculated
            stats.habits.forEach { habitStats ->
                habitStats.currentStreak shouldBe 2  // Two consecutive days
                habitStats.totalCompletions shouldBe 2
            }
        }
    }

    "Property 12: stats are empty when user has no habits" {
        checkAll<Long>(
            5,
            Arb.long(1L..1000L)  // user ID
        ) { userId ->
            // Arrange
            val habitRepository = mock<HabitRepository>()
            val habitRecordRepository = mock<HabitRecordRepository>()
            val statsService = StatsService(habitRecordRepository, habitRepository)
            
            // Mock repository to return empty list
            whenever(habitRepository.findByUserId(userId)).thenReturn(emptyList())
            
            // Act
            val stats = statsService.calculateStats(userId)
            
            // Assert: Stats are empty when user has no habits
            stats.habits.size shouldBe 0
        }
    }
})
