package ase.athlete_view.config.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthFilter(private val userAuthProvider: UserAuthProvider) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null) {
            filterChain.doFilter(request, response)
            return;
        }
        val tokenParts = header.split(" ")
        if (tokenParts.size != 2 || "Bearer" != tokenParts[0]){
            try {
                SecurityContextHolder.getContext().authentication = userAuthProvider.validateToken(tokenParts[1])
            } catch (e: RuntimeException){
                SecurityContextHolder.clearContext();
                throw e;
            }
        }
        filterChain.doFilter(request, response)
    }

}
