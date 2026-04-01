package app.web.bekh20d.habit_tracker.controller

import app.web.bekh20d.habit_tracker.dto.StatsResponse
import app.web.bekh20d.habit_tracker.service.StatsService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/stats")
class StatsController(
    private val statsService: StatsService
) {
    
    @GetMapping
    fun getStats(@AuthenticationPrincipal userId: Long): ResponseEntity<StatsResponse> {
        val stats = statsService.calculateStats(userId)
        return ResponseEntity.ok(stats)
    }
}
