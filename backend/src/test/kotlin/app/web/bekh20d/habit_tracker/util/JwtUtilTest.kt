package app.web.bekh20d.habit_tracker.util

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class JwtUtilTest {

    private lateinit var jwtUtil: JwtUtil
    private val testSecret = "mySecretKeyForTestingPurposesOnly1234567890123456789012345678901234567890"
    private val testExpiration = 3600000L // 1 hour in milliseconds

    @BeforeEach
    fun setUp() {
        jwtUtil = JwtUtil(testSecret, testExpiration)
    }

    @Test
    fun `generateToken should create valid JWT with correct claims`() {
        // Arrange
        val userId = 123L
        val email = "test@example.com"

        // Act
        val token = jwtUtil.generateToken(userId, email)

        // Assert
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        
        // Verify token structure (header.payload.signature)
        val parts = token.split(".")
        assertEquals(3, parts.size, "JWT should have 3 parts separated by dots")
        
        // Verify claims by parsing the token
        val secretKey = Keys.hmacShaKeyFor(testSecret.toByteArray())
        val claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
        
        assertEquals(userId.toString(), claims.subject)
        assertEquals(email, claims["email"])
        assertEquals(userId, claims["userId"].toString().toLong())
        assertNotNull(claims.issuedAt)
        assertNotNull(claims.expiration)
    }

    @Test
    fun `generateToken should include userId and email as claims`() {
        // Arrange
        val userId = 456L
        val email = "user@example.com"

        // Act
        val token = jwtUtil.generateToken(userId, email)

        // Assert
        val extractedUserId = jwtUtil.extractUserId(token)
        val extractedEmail = jwtUtil.extractEmail(token)
        
        assertEquals(userId, extractedUserId)
        assertEquals(email, extractedEmail)
    }

    @Test
    fun `generateToken should set expiration time correctly`() {
        // Arrange
        val userId = 789L
        val email = "expire@example.com"
        val beforeGeneration = System.currentTimeMillis()

        // Act
        val token = jwtUtil.generateToken(userId, email)

        // Assert
        val secretKey = Keys.hmacShaKeyFor(testSecret.toByteArray())
        val claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
        
        val expirationTime = claims.expiration.time
        val expectedExpiration = beforeGeneration + testExpiration
        
        // Allow 1 second tolerance for test execution time
        assertTrue(expirationTime >= expectedExpiration - 1000)
        assertTrue(expirationTime <= expectedExpiration + 1000)
    }

    @Test
    fun `validateToken should return true for valid token`() {
        // Arrange
        val userId = 111L
        val email = "valid@example.com"
        val token = jwtUtil.generateToken(userId, email)

        // Act
        val isValid = jwtUtil.validateToken(token)

        // Assert
        assertTrue(isValid)
    }

    @Test
    fun `validateToken should return false for expired token`() {
        // Arrange
        val userId = 222L
        val email = "expired@example.com"
        
        // Create a JwtUtil with very short expiration (1 millisecond)
        val shortExpirationJwtUtil = JwtUtil(testSecret, 1L)
        val token = shortExpirationJwtUtil.generateToken(userId, email)
        
        // Wait for token to expire
        Thread.sleep(100)

        // Act
        val isValid = jwtUtil.validateToken(token)

        // Assert
        assertFalse(isValid, "Expired token should be invalid")
    }

    @Test
    fun `validateToken should return false for tampered token`() {
        // Arrange
        val userId = 333L
        val email = "tampered@example.com"
        val validToken = jwtUtil.generateToken(userId, email)
        
        // Tamper with the token by modifying a character in the signature
        val parts = validToken.split(".")
        val tamperedSignature = parts[2].replaceFirst('a', 'b')
        val tamperedToken = "${parts[0]}.${parts[1]}.$tamperedSignature"

        // Act
        val isValid = jwtUtil.validateToken(tamperedToken)

        // Assert
        assertFalse(isValid, "Tampered token should be invalid")
    }

    @Test
    fun `validateToken should return false for malformed token`() {
        // Arrange
        val malformedToken = "not.a.valid.jwt.token"

        // Act
        val isValid = jwtUtil.validateToken(malformedToken)

        // Assert
        assertFalse(isValid, "Malformed token should be invalid")
    }

    @Test
    fun `validateToken should return false for token with wrong signature`() {
        // Arrange
        val userId = 444L
        val email = "wrongsig@example.com"
        
        // Create token with different secret
        val differentSecret = "differentSecretKeyForTestingPurposesOnly123456789012345678901234567890"
        val differentJwtUtil = JwtUtil(differentSecret, testExpiration)
        val tokenWithDifferentSecret = differentJwtUtil.generateToken(userId, email)

        // Act - validate with original jwtUtil (different secret)
        val isValid = jwtUtil.validateToken(tokenWithDifferentSecret)

        // Assert
        assertFalse(isValid, "Token signed with different secret should be invalid")
    }

    @Test
    fun `extractUserId should return correct user ID`() {
        // Arrange
        val userId = 555L
        val email = "extract@example.com"
        val token = jwtUtil.generateToken(userId, email)

        // Act
        val extractedUserId = jwtUtil.extractUserId(token)

        // Assert
        assertEquals(userId, extractedUserId)
    }

    @Test
    fun `extractUserId should handle large user IDs`() {
        // Arrange
        val userId = Long.MAX_VALUE
        val email = "largeid@example.com"
        val token = jwtUtil.generateToken(userId, email)

        // Act
        val extractedUserId = jwtUtil.extractUserId(token)

        // Assert
        assertEquals(userId, extractedUserId)
    }

    @Test
    fun `extractEmail should return correct email`() {
        // Arrange
        val userId = 666L
        val email = "correct@example.com"
        val token = jwtUtil.generateToken(userId, email)

        // Act
        val extractedEmail = jwtUtil.extractEmail(token)

        // Assert
        assertEquals(email, extractedEmail)
    }

    @Test
    fun `extractEmail should handle various email formats`() {
        // Arrange
        val userId = 777L
        val emails = listOf(
            "simple@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user_name@sub.example.com"
        )

        emails.forEach { email ->
            // Act
            val token = jwtUtil.generateToken(userId, email)
            val extractedEmail = jwtUtil.extractEmail(token)

            // Assert
            assertEquals(email, extractedEmail, "Email $email should be extracted correctly")
        }
    }

    @Test
    fun `generateToken should create different tokens for same user at different times`() {
        // Arrange
        val userId = 888L
        val email = "same@example.com"

        // Act
        val token1 = jwtUtil.generateToken(userId, email)
        Thread.sleep(1100) // Wait over 1 second to ensure different issuedAt timestamp
        val token2 = jwtUtil.generateToken(userId, email)

        // Assert
        assertNotEquals(token1, token2, "Tokens generated at different times should be different")
        
        // But both should be valid and contain same user data
        assertTrue(jwtUtil.validateToken(token1))
        assertTrue(jwtUtil.validateToken(token2))
        assertEquals(userId, jwtUtil.extractUserId(token1))
        assertEquals(userId, jwtUtil.extractUserId(token2))
        assertEquals(email, jwtUtil.extractEmail(token1))
        assertEquals(email, jwtUtil.extractEmail(token2))
    }

    @Test
    fun `extractUserId should throw exception for invalid token`() {
        // Arrange
        val invalidToken = "invalid.token.here"

        // Act & Assert
        assertThrows<Exception> {
            jwtUtil.extractUserId(invalidToken)
        }
    }

    @Test
    fun `extractEmail should throw exception for invalid token`() {
        // Arrange
        val invalidToken = "invalid.token.here"

        // Act & Assert
        assertThrows<Exception> {
            jwtUtil.extractEmail(invalidToken)
        }
    }

    @Test
    fun `token should contain issuedAt and expiration claims`() {
        // Arrange
        val userId = 999L
        val email = "claims@example.com"
        val beforeGeneration = System.currentTimeMillis()

        // Act
        val token = jwtUtil.generateToken(userId, email)

        // Assert
        val secretKey = Keys.hmacShaKeyFor(testSecret.toByteArray())
        val claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
        
        assertNotNull(claims.issuedAt)
        assertNotNull(claims.expiration)
        assertTrue(claims.issuedAt.time >= beforeGeneration - 1000, "IssuedAt should be around generation time")
        assertTrue(claims.expiration.after(claims.issuedAt), "Expiration should be after issuedAt")
    }

    @Test
    fun `validateToken should handle empty string`() {
        // Arrange
        val emptyToken = ""

        // Act
        val isValid = jwtUtil.validateToken(emptyToken)

        // Assert
        assertFalse(isValid, "Empty token should be invalid")
    }

    @Test
    fun `validateToken should handle null-like strings`() {
        // Arrange
        val nullToken = "null"

        // Act
        val isValid = jwtUtil.validateToken(nullToken)

        // Assert
        assertFalse(isValid, "Null string should be invalid")
    }
}
