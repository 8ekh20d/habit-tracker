package app.web.bekh20d.habit_tracker.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Standalone test for JwtUtil that doesn't depend on Spring context
 */
class JwtUtilStandaloneTest {
    
    private val testSecret = "mySecretKeyForTestingPurposesOnly1234567890123456"
    private val testExpiration = 86400000L // 24 hours
    private val jwtUtil = JwtUtil(testSecret, testExpiration)
    
    @Test
    fun `should generate valid JWT token with userId and email`() {
        val userId = 1L
        val email = "test@example.com"
        
        val token = jwtUtil.generateToken(userId, email)
        
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        assertEquals(3, token.split(".").size, "JWT should have 3 parts: header.payload.signature")
    }
    
    @Test
    fun `should validate correct token`() {
        val token = jwtUtil.generateToken(1L, "test@example.com")
        
        assertTrue(jwtUtil.validateToken(token))
    }
    
    @Test
    fun `should reject invalid token`() {
        val invalidToken = "invalid.token.here"
        
        assertFalse(jwtUtil.validateToken(invalidToken))
    }
    
    @Test
    fun `should extract correct userId from token`() {
        val userId = 42L
        val email = "test@example.com"
        
        val token = jwtUtil.generateToken(userId, email)
        val extractedUserId = jwtUtil.extractUserId(token)
        
        assertEquals(userId, extractedUserId)
    }
    
    @Test
    fun `should extract correct email from token`() {
        val userId = 1L
        val email = "user@example.com"
        
        val token = jwtUtil.generateToken(userId, email)
        val extractedEmail = jwtUtil.extractEmail(token)
        
        assertEquals(email, extractedEmail)
    }
    
    @Test
    fun `should generate unique tokens for same user at different times`() {
        val userId = 1L
        val email = "test@example.com"
        
        val token1 = jwtUtil.generateToken(userId, email)
        Thread.sleep(1000) // 1 second delay to ensure different timestamps
        val token2 = jwtUtil.generateToken(userId, email)
        
        assertNotEquals(token1, token2, "Tokens should be unique due to different issuedAt timestamps")
    }
    
    @Test
    fun `should handle large userId values`() {
        val userId = Long.MAX_VALUE
        val email = "test@example.com"
        
        val token = jwtUtil.generateToken(userId, email)
        val extractedUserId = jwtUtil.extractUserId(token)
        
        assertEquals(userId, extractedUserId)
    }
    
    @Test
    fun `should handle email with special characters`() {
        val userId = 1L
        val email = "test+tag@example.co.uk"
        
        val token = jwtUtil.generateToken(userId, email)
        val extractedEmail = jwtUtil.extractEmail(token)
        
        assertEquals(email, extractedEmail)
    }
}
