package app.web.bekh20d.habit_tracker.dto

import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class CheckHabitRequest(
    @field:NotNull(message = "Date is required")
    val date: LocalDate
)
