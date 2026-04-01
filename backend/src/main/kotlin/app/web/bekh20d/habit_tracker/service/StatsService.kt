package app.web.bekh20d.habit_tracker.service

import app.web.bekh20d.habit_tracker.dto.HabitStats
import app.web.bekh20d.habit_tracker.dto.StatsResponse
import app.web.bekh20d.habit_tracker.model.RecordStatus
import app.web.bekh20d.habit_tracker.repository.HabitRecordRepository
import app.web.bekh20d.habit_tracker.repository.HabitRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class StatsService(
    private val habitRecordRepository: HabitRecordRepository,
    private val habitRepository: HabitRepository
) {
    
    /**
     * Calculate statistics for all habits belonging to a user.
     * 
     * For each habit, dynamically calculates the current streak and counts total completions.
     * 
     * @param userId The ID of the user to calculate statistics for
     * @return StatsResponse containing statistics for all user habits
     */
    fun calculateStats(userId: Long): StatsResponse {
        // Fetch all habits for the user
        val habits = habitRepository.findByUserId(userId)
        
        // Calculate stats for each habit
        val habitStatsList = habits.map { habit ->
            // Calculate streak dynamically
            val currentStreak = calculateStreak(habit.id)
            
            // Count total completions
            val totalCompletions = habitRecordRepository.findByHabitIdOrderByDateDesc(habit.id).size
            
            HabitStats(
                habitId = habit.id,
                habitName = habit.name,
                currentStreak = currentStreak,
                totalCompletions = totalCompletions
            )
        }
        
        return StatsResponse(habits = habitStatsList)
    }
    
    /**
     * Calculate the current streak for a habit.
     * 
     * The streak represents consecutive days from today (or yesterday if today is not completed)
     * where the habit was marked as DONE. Uses LocalDate for timezone-safe date arithmetic.
     * 
     * @param habitId The ID of the habit to calculate streak for
     * @return The current streak count (always >= 0)
     */
    fun calculateStreak(habitId: Long): Int {
        // Step 1: Get all records for this habit, sorted by date descending
        val records = habitRecordRepository.findByHabitIdOrderByDateDesc(habitId)
        
        if (records.isEmpty()) {
            return 0
        }
        
        // Step 2: Initialize streak counter and current date
        var streak = 0
        var currentDate = LocalDate.now()
        
        // Step 3: Traverse backwards from today
        // Loop invariant: currentDate represents the next expected completion date
        for (record in records) {
            // Check if record matches current expected date
            if (record.date == currentDate && record.status == RecordStatus.DONE) {
                streak++
                currentDate = currentDate.minusDays(1)  // Move to previous day
            } else if (record.date.isBefore(currentDate)) {
                // Gap found - streak broken
                break
            }
            // If record.date is after currentDate, skip it (future date)
        }
        
        // Step 4: Handle edge case - if today is not completed, check yesterday
        if (streak == 0 && records.isNotEmpty()) {
            val yesterday = LocalDate.now().minusDays(1)
            if (records[0].date == yesterday && records[0].status == RecordStatus.DONE) {
                streak = 1
                currentDate = yesterday.minusDays(1)
                
                // Continue counting from yesterday
                for (i in 1 until records.size) {
                    val record = records[i]
                    if (record.date == currentDate && record.status == RecordStatus.DONE) {
                        streak++
                        currentDate = currentDate.minusDays(1)
                    } else if (record.date.isBefore(currentDate)) {
                        break
                    }
                }
            }
        }
        
        // Postconditions:
        // - Streak represents consecutive days from today (or yesterday if today not done)
        // - Uses LocalDate for timezone-safe date arithmetic
        // - Streak is always >= 0
        // - Streak never exceeds total number of records
        
        return streak
    }
}
