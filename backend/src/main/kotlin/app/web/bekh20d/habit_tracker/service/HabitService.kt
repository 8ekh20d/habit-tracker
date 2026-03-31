package app.web.bekh20d.habit_tracker.service

import app.web.bekh20d.habit_tracker.exception.NotFoundException
import app.web.bekh20d.habit_tracker.model.FrequencyType
import app.web.bekh20d.habit_tracker.model.Habit
import app.web.bekh20d.habit_tracker.repository.HabitRepository
import org.springframework.stereotype.Service

@Service
class HabitService(
    private val habitRepository: HabitRepository
) {

    fun createHabit(userId: Long, name: String, frequencyType: FrequencyType): Habit {
        if (frequencyType != FrequencyType.DAILY) {
            throw UnsupportedOperationException("Only DAILY frequency is supported in Phase 1")
        }
        
        val habit = Habit(
            userId = userId,
            name = name,
            frequencyType = frequencyType
        )
        
        return habitRepository.save(habit)
    }

    fun getHabits(userId: Long): List<Habit> {
        return habitRepository.findByUserId(userId)
    }

    fun updateHabit(habitId: Long, userId: Long, name: String?): Habit {
        val habit = habitRepository.findByIdAndUserId(habitId, userId)
            ?: throw NotFoundException("Habit not found or access denied")
        
        if (name == null) {
            return habit
        }
        
        val updatedHabit = Habit(
            id = habit.id,
            userId = habit.userId,
            name = name,
            frequencyType = habit.frequencyType,
            createdAt = habit.createdAt
        )
        
        return habitRepository.save(updatedHabit)
    }

    fun deleteHabit(habitId: Long, userId: Long) {
        val habit = habitRepository.findByIdAndUserId(habitId, userId)
            ?: throw NotFoundException("Habit not found or access denied")
        
        habitRepository.delete(habit)
    }
}
