package app.web.bekh20d.habit_tracker.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JwtUtilTest {
    
    private lateinit var jwtUtil: JwtUtil
    private val testSecret = "mySecretKeyForTestingPurposesOnly1234567890123456"
    private val testExpiration = 86400000L // 24 hours
    
    @BeforeEach
    fun setup() {
        jwtUtil = JwtUtil(testSecret, testExpiration)
    }
    
    @Test
    fun `generateToken should create valid JWT token`() {
        val userId = 1L
        val email = "test@example.com"
        
        val token = jwtUtil.generateToken(userId, email)
        
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        assertTrue(token.split(".").size == 3) // JWT has 3 parts: header.payload.signature
    }
    
    @Test
    fun `validateToken should return true for valid token`() {
        val userId = 1L
        val email = "test@example.com"
        
        val token = jwtUtil.generateToken(userId, email)
        
        assertTrue(jwtUtil.validateToken(token))
    }
    
    @Test
    fun `validateToken should return false for invalid token`() {
        val invalidToken = "invalid.token.here"
        
        assertFalse(jwtUtil.validateToken(invalidToken))
    }
    
    @Test
    fun `extractUserId should return correct user ID`() {
        val userId = 42L
        val email = "test@example.com"
        
        val token = jwtUtil.generateToken(userId, email)
        val extractedUserId = jwtUtil.extractUserId(token)
        
        assertEquals(userId, extractedUserId)
    }
    
    @Test
    fun `extractEmail should return correct email`() {
        val userId = 1L
        val email = "user@example.com"
        
        val token = jwtUtil.generateToken(userId, email)
        val extractedEmail = jwtUtil.extractEmail(token)
        
        assertEquals(email, extractedEmail)
    }
    
    @Test
    fun `generated tokens should be unique for same user`() {
        val userId = 1L
        val email = "test@example.com"
        
        val token1 = jwtUtil.generateToken(userId, email)
        Thread.sleep(1000) // 1 second delay to ensure different timestamps
        val token2 = jwtUtil.generateToken(userId, email)
        
        assertNotEquals(token1, token2)
    }
}
