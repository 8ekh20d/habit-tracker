package app.web.bekh20d.habit_tracker.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.property.Arb
import io.kotest.property.arbitrary.email
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

/**
 * Property-Based Tests for JWT Token Validity
 * 
 * **Validates: Requirements 3.4, 3.5, 3.6, 3.7, 12.3, 12.4**
 * 
 * Property 6: JWT Token Validity
 * - Generated tokens contain correct claims (userId, email)
 * - Tokens have valid signatures
 * - Token validation works correctly
 */
class JwtUtilPropertyTest : StringSpec({

    val testSecret = "mySecretKeyForTestingPurposesOnly1234567890123456"
    val testExpiration = 86400000L // 24 hours
    val jwtUtil = JwtUtil(testSecret, testExpiration)

    "Property 6: generated tokens contain correct userId claim" {
        checkAll(10, Arb.long(1..Long.MAX_VALUE), Arb.email()) { userId, email ->
            val token = jwtUtil.generateToken(userId, email)
            val extractedUserId = jwtUtil.extractUserId(token)
            
            extractedUserId shouldBe userId
        }
    }

    "Property 6: generated tokens contain correct email claim" {
        checkAll(10, Arb.long(1..Long.MAX_VALUE), Arb.email()) { userId, email ->
            val token = jwtUtil.generateToken(userId, email)
            val extractedEmail = jwtUtil.extractEmail(token)
            
            extractedEmail shouldBe email
        }
    }

    "Property 6: generated tokens have valid signatures" {
        checkAll(10, Arb.long(1..Long.MAX_VALUE), Arb.email()) { userId, email ->
            val token = jwtUtil.generateToken(userId, email)
            
            // Token should be valid (signature verification passes)
            jwtUtil.validateToken(token) shouldBe true
        }
    }

    "Property 6: token validation rejects tokens with invalid signatures" {
        checkAll(10, Arb.long(1..Long.MAX_VALUE), Arb.email()) { userId, email ->
            val token = jwtUtil.generateToken(userId, email)
            
            // Tamper with the token by modifying the signature
            val parts = token.split(".")
            val tamperedToken = "${parts[0]}.${parts[1]}.invalidSignature"
            
            // Tampered token should be invalid
            jwtUtil.validateToken(tamperedToken) shouldBe false
        }
    }

    "Property 6: tokens have correct JWT structure (header.payload.signature)" {
        checkAll(10, Arb.long(1..Long.MAX_VALUE), Arb.email()) { userId, email ->
            val token = jwtUtil.generateToken(userId, email)
            
            // JWT should have exactly 3 parts separated by dots
            val parts = token.split(".")
            parts.size shouldBe 3
            
            // Each part should be non-empty
            parts[0].isNotEmpty() shouldBe true
            parts[1].isNotEmpty() shouldBe true
            parts[2].isNotEmpty() shouldBe true
        }
    }

    "Property 6: multiple tokens for same user are all valid" {
        checkAll(10, Arb.long(1..Long.MAX_VALUE), Arb.email()) { userId, email ->
            val token1 = jwtUtil.generateToken(userId, email)
            val token2 = jwtUtil.generateToken(userId, email)
            
            // Both tokens should be valid
            jwtUtil.validateToken(token1) shouldBe true
            jwtUtil.validateToken(token2) shouldBe true
        }
    }

    "Property 6: tokens preserve userId and email claims correctly" {
        checkAll(10, Arb.long(1..Long.MAX_VALUE), Arb.email()) { userId, email ->
            val token = jwtUtil.generateToken(userId, email)
            
            // Both userId and email should be extractable and correct
            jwtUtil.extractUserId(token) shouldBe userId
            jwtUtil.extractEmail(token) shouldBe email
        }
    }

    "Property 6: token validation works correctly for edge case userIds" {
        val edgeCaseUserIds = listOf(1L, Long.MAX_VALUE, 999999999L)
        
        edgeCaseUserIds.forEach { userId ->
            checkAll(10, Arb.email()) { email ->
                val token = jwtUtil.generateToken(userId, email)
                
                jwtUtil.validateToken(token) shouldBe true
                jwtUtil.extractUserId(token) shouldBe userId
                jwtUtil.extractEmail(token) shouldBe email
            }
        }
    }

    "Property 6: completely invalid tokens are rejected" {
        val invalidTokens = listOf(
            "not.a.token",
            "invalid",
            "",
            "a.b",
            "a.b.c.d",
            "eyJhbGciOiJIUzI1NiJ9.invalid.signature"
        )
        
        invalidTokens.forEach { invalidToken ->
            jwtUtil.validateToken(invalidToken) shouldBe false
        }
    }
})
