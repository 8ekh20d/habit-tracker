package app.web.bekh20d.habit_tracker.service

import app.web.bekh20d.habit_tracker.exception.NotFoundException
import app.web.bekh20d.habit_tracker.model.FrequencyType
import app.web.bekh20d.habit_tracker.model.Habit
import app.web.bekh20d.habit_tracker.model.HabitRecord
import app.web.bekh20d.habit_tracker.model.RecordStatus
import app.web.bekh20d.habit_tracker.repository.HabitRecordRepository
import app.web.bekh20d.habit_tracker.repository.HabitRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class HabitService(
    private val habitRepository: HabitRepository,
    private val habitRecordRepository: HabitRecordRepository
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
        
        // Delete all associated habit records first (cascading deletion)
        val records = habitRecordRepository.findByHabitIdOrderByDateDesc(habitId)
        habitRecordRepository.deleteAll(records)
        
        // Then delete the habit
        habitRepository.delete(habit)
    }

    fun checkHabit(habitId: Long, userId: Long, date: LocalDate): HabitRecord {
        // Verify habit ownership
        val habit = habitRepository.findByIdAndUserId(habitId, userId)
            ?: throw NotFoundException("Habit not found or access denied")
        
        // Check if record already exists for this date
        val existingRecord = habitRecordRepository.findByHabitIdAndDate(habitId, date)
        
        // Upsert logic: update if exists, create if not
        val record = if (existingRecord != null) {
            HabitRecord(
                id = existingRecord.id,
                habitId = habitId,
                date = date,
                status = RecordStatus.DONE
            )
        } else {
            HabitRecord(
                habitId = habitId,
                date = date,
                status = RecordStatus.DONE
            )
        }
        
        return habitRecordRepository.save(record)
    }

    fun getHabitRecords(userId: Long): List<HabitRecord> {
        // Get all habits for the user
        val habits = habitRepository.findByUserId(userId)
        val habitIds = habits.map { it.id }
        
        // Get all records for these habits
        return habitIds.flatMap { habitId ->
            habitRecordRepository.findByHabitIdOrderByDateDesc(habitId)
        }
    }
}
