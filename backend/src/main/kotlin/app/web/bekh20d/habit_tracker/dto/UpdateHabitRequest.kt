package app.web.bekh20d.habit_tracker.dto

import jakarta.validation.constraints.Size

data class UpdateHabitRequest(
    @field:Size(max = 100, message = "Habit name must not exceed 100 characters")
    val name: String?
)
