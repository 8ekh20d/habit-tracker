package app.web.bekh20d.habit_tracker.dto

import java.time.LocalDate

data class HabitRecordResponse(
    val habitId: Long,
    val date: LocalDate,
    val status: String
)
