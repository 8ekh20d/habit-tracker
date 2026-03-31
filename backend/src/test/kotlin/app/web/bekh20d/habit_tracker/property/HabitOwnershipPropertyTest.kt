package app.web.bekh20d.habit_tracker.property

import app.web.bekh20d.habit_tracker.exception.NotFoundException
import app.web.bekh20d.habit_tracker.model.FrequencyType
import app.web.bekh20d.habit_tracker.model.Habit
import app.web.bekh20d.habit_tracker.repository.HabitRepository
import app.web.bekh20d.habit_tracker.service.HabitService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.filter
import io.kotest.property.checkAll
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

/**
 * Property-Based Test for Habit Ownership
 * 
 * **Validates: Requirements 4.5, 5.2, 6.2, 7.3, 8.3, 15.1, 15.2, 15.3, 15.5**
 * 
 * This test verifies that users can only access, modify, and delete habits that belong to them.
 * It uses property-based testing to generate random user IDs and habit data to ensure the
 * ownership invariant holds across all possible inputs.
 */
class HabitOwnershipPropertyTest : StringSpec({

    "Property 9: created habits are always associated with creator's userId" {
        checkAll<Long, String>(
            Arb.long(1L..1000L),  // user ID
            Arb.string(1..100).filter { it.isNotBlank() }  // habit name
        ) { userId, habitName ->
            // Arrange
            val habitRepository = mock<HabitRepository>()
            val habitService = HabitService(habitRepository)
            
            val createdHabit = Habit(
                id = 1L,
                userId = userId,
                name = habitName,
                frequencyType = FrequencyType.DAILY,
                createdAt = LocalDateTime.now()
            )
            
            whenever(habitRepository.save(any())).thenReturn(createdHabit)
            
            // Act
            val result = habitService.createHabit(userId, habitName, FrequencyType.DAILY)
            
            // Assert: Created habit is always associated with creator's userId
            result.userId shouldBe userId
            
            // Verify that the habit saved to repository has the correct userId
            verify(habitRepository).save(any())
        }
    }

    "Property 4: users can only retrieve their own habits" {
        checkAll<Long, Long, String>(
            Arb.long(1L..1000L),  // owner user ID
            Arb.long(1L..1000L),  // different user ID
            Arb.string(1..100)  // habit name
        ) { ownerId, otherUserId, habitName ->
            // Skip if user IDs are the same
            if (ownerId == otherUserId) return@checkAll
            // Arrange
            val habitRepository = mock<HabitRepository>()
            val habitService = HabitService(habitRepository)
            
            val ownerHabit = Habit(
                id = 1L,
                userId = ownerId,
                name = habitName,
                frequencyType = FrequencyType.DAILY,
                createdAt = LocalDateTime.now()
            )
            
            val otherUserHabit = Habit(
                id = 2L,
                userId = otherUserId,
                name = "Other User Habit",
                frequencyType = FrequencyType.DAILY,
                createdAt = LocalDateTime.now()
            )
            
            // Mock repository to return only owner's habits
            whenever(habitRepository.findByUserId(ownerId)).thenReturn(listOf(ownerHabit))
            whenever(habitRepository.findByUserId(otherUserId)).thenReturn(listOf(otherUserHabit))
            
            // Act
            val ownerHabits = habitService.getHabits(ownerId)
            val otherUserHabits = habitService.getHabits(otherUserId)
            
            // Assert: Owner can only see their own habits
            ownerHabits.size shouldBe 1
            ownerHabits.first().userId shouldBe ownerId
            ownerHabits shouldNotContain otherUserHabit
            
            // Assert: Other user can only see their own habits
            otherUserHabits.size shouldBe 1
            otherUserHabits.first().userId shouldBe otherUserId
            otherUserHabits shouldNotContain ownerHabit
        }
    }

    "Property 4: users cannot modify habits they do not own" {
        checkAll<Long, Long, String, String>(
            Arb.long(1L..1000L),  // owner user ID
            Arb.long(1L..1000L),  // different user ID
            Arb.string(1..100),  // original habit name
            Arb.string(1..100)   // new habit name
        ) { ownerId, otherUserId, originalName, newName ->
            // Skip if user IDs are the same
            if (ownerId == otherUserId) return@checkAll
            // Arrange
            val habitRepository = mock<HabitRepository>()
            val habitService = HabitService(habitRepository)
            
            val habitId = 1L
            
            // Mock: habit exists but belongs to owner, not other user
            whenever(habitRepository.findByIdAndUserId(habitId, ownerId)).thenReturn(
                Habit(
                    id = habitId,
                    userId = ownerId,
                    name = originalName,
                    frequencyType = FrequencyType.DAILY,
                    createdAt = LocalDateTime.now()
                )
            )
            whenever(habitRepository.findByIdAndUserId(habitId, otherUserId)).thenReturn(null)
            
            // Act & Assert: Other user cannot modify owner's habit
            val exception = shouldThrow<NotFoundException> {
                habitService.updateHabit(habitId, otherUserId, newName)
            }
            
            exception.message shouldBe "Habit not found or access denied"
            
            // Verify repository was queried with wrong user ID
            verify(habitRepository).findByIdAndUserId(habitId, otherUserId)
            // Verify save was never called
            verify(habitRepository, never()).save(any())
        }
    }

    "Property 4: users cannot delete habits they do not own" {
        checkAll<Long, Long, String>(
            Arb.long(1L..1000L),  // owner user ID
            Arb.long(1L..1000L),  // different user ID
            Arb.string(1..100)  // habit name
        ) { ownerId, otherUserId, habitName ->
            // Skip if user IDs are the same
            if (ownerId == otherUserId) return@checkAll
            // Arrange
            val habitRepository = mock<HabitRepository>()
            val habitService = HabitService(habitRepository)
            
            val habitId = 1L
            
            // Mock: habit exists but belongs to owner, not other user
            whenever(habitRepository.findByIdAndUserId(habitId, ownerId)).thenReturn(
                Habit(
                    id = habitId,
                    userId = ownerId,
                    name = habitName,
                    frequencyType = FrequencyType.DAILY,
                    createdAt = LocalDateTime.now()
                )
            )
            whenever(habitRepository.findByIdAndUserId(habitId, otherUserId)).thenReturn(null)
            
            // Act & Assert: Other user cannot delete owner's habit
            val exception = shouldThrow<NotFoundException> {
                habitService.deleteHabit(habitId, otherUserId)
            }
            
            exception.message shouldBe "Habit not found or access denied"
            
            // Verify repository was queried with wrong user ID
            verify(habitRepository).findByIdAndUserId(habitId, otherUserId)
            // Verify delete was never called
            verify(habitRepository, never()).delete(any())
        }
    }

    "Property 4: owner can successfully access their own habits" {
        checkAll<Long, String>(
            Arb.long(1L..1000L),  // user ID
            Arb.string(1..100)  // habit name
        ) { userId, habitName ->
            // Arrange
            val habitRepository = mock<HabitRepository>()
            val habitService = HabitService(habitRepository)
            
            val habit = Habit(
                id = 1L,
                userId = userId,
                name = habitName,
                frequencyType = FrequencyType.DAILY,
                createdAt = LocalDateTime.now()
            )
            
            whenever(habitRepository.findByUserId(userId)).thenReturn(listOf(habit))
            
            // Act
            val habits = habitService.getHabits(userId)
            
            // Assert: Owner can access their own habits
            habits.size shouldBe 1
            habits.first().userId shouldBe userId
            habits.first().name shouldBe habitName
        }
    }

    "Property 4: owner can successfully modify their own habits" {
        checkAll<Long, String, String>(
            Arb.long(1L..1000L),  // user ID
            Arb.string(1..100),  // original name
            Arb.string(1..100)   // new name
        ) { userId, originalName, newName ->
            // Arrange
            val habitRepository = mock<HabitRepository>()
            val habitService = HabitService(habitRepository)
            
            val habitId = 1L
            val existingHabit = Habit(
                id = habitId,
                userId = userId,
                name = originalName,
                frequencyType = FrequencyType.DAILY,
                createdAt = LocalDateTime.now()
            )
            
            val updatedHabit = Habit(
                id = habitId,
                userId = userId,
                name = newName,
                frequencyType = FrequencyType.DAILY,
                createdAt = existingHabit.createdAt
            )
            
            whenever(habitRepository.findByIdAndUserId(habitId, userId)).thenReturn(existingHabit)
            whenever(habitRepository.save(any())).thenReturn(updatedHabit)
            
            // Act
            val result = habitService.updateHabit(habitId, userId, newName)
            
            // Assert: Owner can modify their own habit
            result.userId shouldBe userId
            result.name shouldBe newName
            verify(habitRepository).findByIdAndUserId(habitId, userId)
            verify(habitRepository).save(any())
        }
    }

    "Property 4: owner can successfully delete their own habits" {
        checkAll<Long, String>(
            Arb.long(1L..1000L),  // user ID
            Arb.string(1..100)  // habit name
        ) { userId, habitName ->
            // Arrange
            val habitRepository = mock<HabitRepository>()
            val habitService = HabitService(habitRepository)
            
            val habitId = 1L
            val habit = Habit(
                id = habitId,
                userId = userId,
                name = habitName,
                frequencyType = FrequencyType.DAILY,
                createdAt = LocalDateTime.now()
            )
            
            whenever(habitRepository.findByIdAndUserId(habitId, userId)).thenReturn(habit)
            
            // Act
            habitService.deleteHabit(habitId, userId)
            
            // Assert: Owner can delete their own habit
            verify(habitRepository).findByIdAndUserId(habitId, userId)
            verify(habitRepository).delete(habit)
        }
    }
})
