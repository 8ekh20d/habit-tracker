package app.web.bekh20d.habit_tracker.dto

data class HabitStats(
    val habitId: Long,
    val habitName: String,
    val currentStreak: Int,
    val totalCompletions: Int
)
