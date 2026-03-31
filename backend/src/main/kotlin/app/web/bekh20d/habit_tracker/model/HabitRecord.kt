package app.web.bekh20d.habit_tracker.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

@Entity
@Table(
    name = "habit_records",
    uniqueConstraints = [UniqueConstraint(columnNames = ["habit_id", "date"])]
)
class HabitRecord(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "habit_id", nullable = false)
    val habitId: Long,

    @Column(name = "date", nullable = false)
    val date: LocalDate,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    val status: RecordStatus = RecordStatus.DONE
)
