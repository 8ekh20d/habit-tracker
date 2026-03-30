package app.web.bekh20d.habit_tracker.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${app.frontend.url:http://localhost:3000}") private val frontendUrl: String
) {
    
    private val logger = LoggerFactory.getLogger(EmailService::class.java)
    
    fun sendVerificationEmail(email: String, token: String) {
        try {
            val verificationLink = "$frontendUrl/verify-email?token=$token"
            
            val message = SimpleMailMessage()
            message.setTo(email)
            message.subject = "Verify Your Email - Habit Tracker"
            message.text = """
                Welcome to Habit Tracker!
                
                Please verify your email address by clicking the link below:
                
                $verificationLink
                
                This link will expire in 24 hours.
                
                If you did not create an account, please ignore this email.
            """.trimIndent()
            
            mailSender.send(message)
            logger.info("Verification email sent to: $email")
        } catch (e: Exception) {
            logger.error("Failed to send verification email to: $email", e)
            // Don't fail signup if email sending fails
        }
    }
}
