package app.web.bekh20d.habit_tracker.service

import app.web.bekh20d.habit_tracker.exception.NotFoundException
import app.web.bekh20d.habit_tracker.model.FrequencyType
import app.web.bekh20d.habit_tracker.model.Habit
import app.web.bekh20d.habit_tracker.repository.HabitRecordRepository
import app.web.bekh20d.habit_tracker.repository.HabitRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class HabitServiceTest {

    @Mock
    private lateinit var habitRepository: HabitRepository

    @Mock
    private lateinit var habitRecordRepository: HabitRecordRepository

    @InjectMocks
    private lateinit var habitService: HabitService

    @Test
    fun `createHabit should create habit with DAILY frequency`() {
        // Arrange
        val userId = 1L
        val name = "Morning Exercise"
        val frequencyType = FrequencyType.DAILY
        
        val savedHabit = Habit(
            id = 1L,
            userId = userId,
            name = name,
            frequencyType = frequencyType,
            createdAt = LocalDateTime.now()
        )

        `when`(habitRepository.save(any(Habit::class.java))).thenReturn(savedHabit)

        // Act
        val result = habitService.createHabit(userId, name, frequencyType)

        // Assert
        assertEquals(savedHabit.id, result.id)
        assertEquals(userId, result.userId)
        assertEquals(name, result.name)
        assertEquals(FrequencyType.DAILY, result.frequencyType)
        verify(habitRepository, times(1)).save(any(Habit::class.java))
    }

    @Test
    fun `getHabits should return all habits for user`() {
        // Arrange
        val userId = 1L
        val habits = listOf(
            Habit(id = 1L, userId = userId, name = "Exercise", frequencyType = FrequencyType.DAILY),
            Habit(id = 2L, userId = userId, name = "Read", frequencyType = FrequencyType.DAILY)
        )

        `when`(habitRepository.findByUserId(userId)).thenReturn(habits)

        // Act
        val result = habitService.getHabits(userId)

        // Assert
        assertEquals(2, result.size)
        assertEquals(habits, result)
        verify(habitRepository, times(1)).findByUserId(userId)
    }

    @Test
    fun `getHabits should return empty list when user has no habits`() {
        // Arrange
        val userId = 1L

        `when`(habitRepository.findByUserId(userId)).thenReturn(emptyList())

        // Act
        val result = habitService.getHabits(userId)

        // Assert
        assertTrue(result.isEmpty())
        verify(habitRepository, times(1)).findByUserId(userId)
    }

    @Test
    fun `updateHabit should update habit name when user owns habit`() {
        // Arrange
        val habitId = 1L
        val userId = 1L
        val oldName = "Old Name"
        val newName = "New Name"
        
        val existingHabit = Habit(
            id = habitId,
            userId = userId,
            name = oldName,
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

        `when`(habitRepository.findByIdAndUserId(habitId, userId)).thenReturn(existingHabit)
        `when`(habitRepository.save(any(Habit::class.java))).thenReturn(updatedHabit)

        // Act
        val result = habitService.updateHabit(habitId, userId, newName)

        // Assert
        assertEquals(newName, result.name)
        assertEquals(habitId, result.id)
        verify(habitRepository, times(1)).findByIdAndUserId(habitId, userId)
        verify(habitRepository, times(1)).save(any(Habit::class.java))
    }

    @Test
    fun `updateHabit should return unchanged habit when name is null`() {
        // Arrange
        val habitId = 1L
        val userId = 1L
        
        val existingHabit = Habit(
            id = habitId,
            userId = userId,
            name = "Exercise",
            frequencyType = FrequencyType.DAILY,
            createdAt = LocalDateTime.now()
        )

        `when`(habitRepository.findByIdAndUserId(habitId, userId)).thenReturn(existingHabit)

        // Act
        val result = habitService.updateHabit(habitId, userId, null)

        // Assert
        assertEquals(existingHabit, result)
        verify(habitRepository, times(1)).findByIdAndUserId(habitId, userId)
        verify(habitRepository, never()).save(any(Habit::class.java))
    }

    @Test
    fun `updateHabit should throw NotFoundException when habit not owned by user`() {
        // Arrange
        val habitId = 1L
        val userId = 1L
        val newName = "New Name"

        `when`(habitRepository.findByIdAndUserId(habitId, userId)).thenReturn(null)

        // Act & Assert
        val exception = assertThrows(NotFoundException::class.java) {
            habitService.updateHabit(habitId, userId, newName)
        }
        
        assertEquals("Habit not found or access denied", exception.message)
        verify(habitRepository, times(1)).findByIdAndUserId(habitId, userId)
        verify(habitRepository, never()).save(any(Habit::class.java))
    }

    @Test
    fun `deleteHabit should delete habit when user owns it`() {
        // Arrange
        val habitId = 1L
        val userId = 1L
        
        val existingHabit = Habit(
            id = habitId,
            userId = userId,
            name = "Exercise",
            frequencyType = FrequencyType.DAILY,
            createdAt = LocalDateTime.now()
        )

        `when`(habitRepository.findByIdAndUserId(habitId, userId)).thenReturn(existingHabit)

        // Act
        habitService.deleteHabit(habitId, userId)

        // Assert
        verify(habitRepository, times(1)).findByIdAndUserId(habitId, userId)
        verify(habitRepository, times(1)).delete(existingHabit)
    }

    @Test
    fun `deleteHabit should throw NotFoundException when habit not owned by user`() {
        // Arrange
        val habitId = 1L
        val userId = 1L

        `when`(habitRepository.findByIdAndUserId(habitId, userId)).thenReturn(null)

        // Act & Assert
        val exception = assertThrows(NotFoundException::class.java) {
            habitService.deleteHabit(habitId, userId)
        }
        
        assertEquals("Habit not found or access denied", exception.message)
        verify(habitRepository, times(1)).findByIdAndUserId(habitId, userId)
        verify(habitRepository, never()).delete(any(Habit::class.java))
    }
}
