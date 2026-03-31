package app.web.bekh20d.habit_tracker.repository

import app.web.bekh20d.habit_tracker.model.HabitRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface HabitRecordRepository : JpaRepository<HabitRecord, Long> {
    
    fun findByHabitIdAndDate(habitId: Long, date: LocalDate): HabitRecord?
    
    fun findByHabitIdOrderByDateDesc(habitId: Long): List<HabitRecord>
}
