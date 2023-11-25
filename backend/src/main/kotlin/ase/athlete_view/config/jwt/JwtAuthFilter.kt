package ase.athlete_view.config.jwt

import ase.athlete_view.domain.authentication.service.AuthenticationService
import ase.athlete_view.domain.user.pojo.dto.UserDto
import io.github.oshai.kotlinlogging.KLogger
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthFilter(
    private val userAuthProvider: UserAuthProvider,
    private val authenticationService: AuthenticationService,
    private val log: KLogger
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        log.info { "Entered JWT Section" }
        val header = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.info { "Header value is '$header'"}
        if (header == null) {
            logger.info { "Header not found..." }
            filterChain.doFilter(request, response)
            return;
        }
        val tokenParts = header.split(" ")
        if (tokenParts.size == 2 && "Bearer" == tokenParts[0]) {
            try {
                log.info { "Attempting to authenticate" }
                val auth = userAuthProvider.validateToken(tokenParts[1])
                SecurityContextHolder.getContext().authentication = auth
                // in this context we know, that the token is legit and not expired.
                (auth.principal as UserDto).id?.let { this.updateJwtHeader(it, response) }
            } catch (e: RuntimeException) {
                this.log.warn { "Could not set Auth from JWT. Maybe it is expired?" }
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun updateJwtHeader(userId: Long, response: HttpServletResponse) {
        response.setHeader(HttpHeaders.AUTHORIZATION, this.authenticationService.createJwtToken(userId))
    }
}
