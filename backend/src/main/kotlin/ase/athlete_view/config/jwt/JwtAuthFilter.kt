/*-
 * #%L
 * athlete_view
 * %%
 * Copyright (C) 2023 - 2024 TU Wien INSO ASE GROUP 5 WS2023
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package ase.athlete_view.config.jwt

import ase.athlete_view.domain.authentication.service.AuthService
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import io.github.oshai.kotlinlogging.KLogger
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthFilter(
    private val userAuthProvider: UserAuthProvider,
    private val authService: AuthService,
    private val log: KLogger
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header == null) {
            log.debug { "Header not found..." }
            filterChain.doFilter(request, response)
            return
        }
        val tokenParts = header.split(" ")
        if (tokenParts.size == 2 && "Bearer" == tokenParts[0]) {
            try {
                log.debug { "Attempting to authenticate" }
                val auth = userAuthProvider.validateToken(tokenParts[1])
                SecurityContextHolder.getContext().authentication = auth
                // in this context we know, that the token is legit and not expired.
                (auth.principal as UserDTO).id?.let { this.updateJwtHeader(it, response) }
            } catch (e: RuntimeException) {
                this.log.warn { "Could not set Auth from JWT. Maybe it is expired?" }
                SecurityContextHolder.clearContext()
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun updateJwtHeader(userId: Long, response: HttpServletResponse) {
        // otherwise, the front end http interceptors cannot read the new token
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "authorization")
        response.setHeader(HttpHeaders.AUTHORIZATION, this.authService.createJwtToken(userId))
    }
}
