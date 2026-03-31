package app.web.bekh20d.habit_tracker.repository

import app.web.bekh20d.habit_tracker.model.Habit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HabitRepository : JpaRepository<Habit, Long> {
    fun findByIdAndUserId(id: Long, userId: Long): Habit?
    fun findByUserId(userId: Long): List<Habit>
}
