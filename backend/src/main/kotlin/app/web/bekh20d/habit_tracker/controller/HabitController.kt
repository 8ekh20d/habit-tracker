package app.web.bekh20d.habit_tracker.controller

import app.web.bekh20d.habit_tracker.dto.CheckHabitRequest
import app.web.bekh20d.habit_tracker.dto.CreateHabitRequest
import app.web.bekh20d.habit_tracker.dto.HabitRecordResponse
import app.web.bekh20d.habit_tracker.dto.HabitResponse
import app.web.bekh20d.habit_tracker.dto.UpdateHabitRequest
import app.web.bekh20d.habit_tracker.model.FrequencyType
import app.web.bekh20d.habit_tracker.service.HabitService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/habits")
class HabitController(
    private val habitService: HabitService
) {

    @GetMapping
    fun getHabits(@AuthenticationPrincipal userId: Long): ResponseEntity<List<HabitResponse>> {
        val habits = habitService.getHabits(userId)
        val response = habits.map { habit ->
            HabitResponse(
                id = habit.id,
                name = habit.name,
                frequencyType = habit.frequencyType.name,
                createdAt = habit.createdAt.toString()
            )
        }
        return ResponseEntity.ok(response)
    }

    @PostMapping
    fun createHabit(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: CreateHabitRequest
    ): ResponseEntity<HabitResponse> {
        val habit = habitService.createHabit(userId, request.name, FrequencyType.DAILY)
        val response = HabitResponse(
            id = habit.id,
            name = habit.name,
            frequencyType = habit.frequencyType.name,
            createdAt = habit.createdAt.toString()
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PatchMapping("/{id}")
    fun updateHabit(
        @PathVariable id: Long,
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: UpdateHabitRequest
    ): ResponseEntity<HabitResponse> {
        val habit = habitService.updateHabit(id, userId, request.name)
        val response = HabitResponse(
            id = habit.id,
            name = habit.name,
            frequencyType = habit.frequencyType.name,
            createdAt = habit.createdAt.toString()
        )
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deleteHabit(
        @PathVariable id: Long,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<Void> {
        habitService.deleteHabit(id, userId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/check")
    fun checkHabit(
        @PathVariable id: Long,
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: CheckHabitRequest
    ): ResponseEntity<HabitRecordResponse> {
        val record = habitService.checkHabit(id, userId, request.date)
        val response = HabitRecordResponse(
            habitId = record.habitId,
            date = record.date,
            status = record.status.name
        )
        return ResponseEntity.ok(response)
    }
}
