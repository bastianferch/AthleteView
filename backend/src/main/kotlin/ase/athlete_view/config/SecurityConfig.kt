package ase.athlete_view.config

import ase.athlete_view.config.jwt.JwtAuthFilter
import ase.athlete_view.config.jwt.UserAuthProvider
import ase.athlete_view.domain.authentication.service.AuthService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter


@Configuration
@EnableWebSecurity
class SecurityConfig(private val userAuthProvider: UserAuthProvider, private val authService: AuthService) {
    private val logger = KotlinLogging.logger {}

    @Order(1)
    @Bean
    fun apiFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { csrf -> csrf.disable() }
            .addFilterBefore(JwtAuthFilter(userAuthProvider, authService, logger), BasicAuthenticationFilter::class.java)
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("api/health").permitAll()
                    .requestMatchers("api/health/**").permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement { sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(10)
    }
}
