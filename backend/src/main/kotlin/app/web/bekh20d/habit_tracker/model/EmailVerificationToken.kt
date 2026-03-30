package app.web.bekh20d.habit_tracker.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "email_verification_tokens")
class EmailVerificationToken(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "token", unique = true, nullable = false)
    val token: String,

    @Column(name = "expiry_date", nullable = false)
    val expiryDate: LocalDateTime
)
