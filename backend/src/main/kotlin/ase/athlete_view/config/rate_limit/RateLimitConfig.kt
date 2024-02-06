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
package ase.athlete_view.config.rate_limit

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.Duration

//rate limiting is not applied for tests, except the rateLimit tests
@Configuration
@Profile("!test | rateLimitTest")
class RateLimitConfig: WebMvcConfigurer {

    val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var failedLoginInterceptor: FailedLoginInterceptor

    // this function adds http interceptors for rate limiting
    override fun addInterceptors(registry: InterceptorRegistry) {
        log.trace { "RateLimitConfig.addInterceptors($registry)" }

        // login interceptor: suspend account after 5 failed login attempts
        registry.addInterceptor(failedLoginInterceptor)
            .addPathPatterns("/api/auth/login")

        // general interceptor: no more than 15 requests per second
        registry.addInterceptor(RateLimitInterceptor(15, Duration.ofSeconds(1)))
            .addPathPatterns("/**")

        // for everything where mails are sent: once every minute
        registry.addInterceptor(RateLimitInterceptor(1, Duration.ofMinutes(1)))
            .addPathPatterns("/api/auth/confirmation/new")
        registry.addInterceptor(RateLimitInterceptor(1, Duration.ofMinutes(1)))
            .addPathPatterns("/api/auth/forgot-password")
        registry.addInterceptor(RateLimitInterceptor(5, Duration.ofMinutes(1)))
            .addPathPatterns("/api/user/trainer/invitation")
        registry.addInterceptor(RateLimitInterceptor(1, Duration.ofMinutes(1)))
            .addPathPatterns("/api/auth/registration/**")

        // for syncing with the garmin API:
        registry.addInterceptor(RateLimitInterceptor(1, Duration.ofSeconds(5)))
            .addPathPatterns("/api/activity/sync")
        registry.addInterceptor(RateLimitInterceptor(1, Duration.ofSeconds(5)))
            .addPathPatterns("/api/health/sync")
    }
}
