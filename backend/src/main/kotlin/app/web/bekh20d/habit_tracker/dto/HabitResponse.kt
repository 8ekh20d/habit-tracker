package app.web.bekh20d.habit_tracker.dto

data class HabitResponse(
    val id: Long,
    val name: String,
    val frequencyType: String,
    val createdAt: String
)
