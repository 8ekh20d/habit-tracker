package app.web.bekh20d.habit_tracker.config

import app.web.bekh20d.habit_tracker.util.JwtUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = extractTokenFromRequest(request)

            if (token != null && jwtUtil.validateToken(token)) {
                val userId = jwtUtil.extractUserId(token)
                
                // Create authentication with userId as principal
                val authentication = UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    emptyList()
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                
                // Set authentication in SecurityContext
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            // Log error but don't block the filter chain
            logger.error("Cannot set user authentication: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }

    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}
