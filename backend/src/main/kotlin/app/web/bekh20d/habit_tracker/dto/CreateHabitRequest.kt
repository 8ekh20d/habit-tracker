package app.web.bekh20d.habit_tracker.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateHabitRequest(
    @field:NotBlank(message = "Habit name is required")
    @field:Size(max = 100, message = "Habit name must not exceed 100 characters")
    val name: String
)
