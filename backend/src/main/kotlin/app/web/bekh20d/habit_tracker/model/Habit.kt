package app.web.bekh20d.habit_tracker.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Entity
@Table(name = "habits")
class Habit(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "name", nullable = false)
    @field:NotBlank(message = "Habit name is required")
    @field:Size(max = 100, message = "Habit name must not exceed 100 characters")
    val name: String,

    @Column(name = "frequency_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val frequencyType: FrequencyType = FrequencyType.DAILY,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
