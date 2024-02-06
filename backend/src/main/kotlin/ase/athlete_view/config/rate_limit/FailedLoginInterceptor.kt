package ase.athlete_view.config.rate_limit

import ase.athlete_view.common.exception.entity.RateLimitExceededException
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class FailedLoginInterceptor: HandlerInterceptor {

    val log = KotlinLogging.logger {}

    private val cache: MutableMap<String, Bucket> = ConcurrentHashMap();

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


    // there can be 5 failed attempts per day
    private final val duration = Duration.ofMinutes(15);
    private final val allowedAttemptsInDuration = 5L;

    // checks if this user has been suspended because of failed login attempts.
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        log.trace { "FailedLoginInterceptor.preHandle($request, $response, $handler)" }
        val userId = RateLimitUtils.getId(request)
        if (isAllowed(userId)) {
            return true
        } else {
            throw RateLimitExceededException("Too many failed login attempts. IP address has been suspended for 15 minutes.")
        }
    }

    //if this method is called, it means that the authentication was successful.
    //the counter for this IP address can therefore be reset
    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        log.trace { "FailedLoginInterceptor.postHandle($request, $response, $handler, $modelAndView)" }
        val tokenBucket: Bucket = resolveBucket(RateLimitUtils.getId(request))
        tokenBucket.reset()
    }

    // called by the global exception handler when there is a failed login attempt.
    // increases the number of failed attempts by this user by 1.
    fun onFailedLogin(request: HttpServletRequest) {
        log.trace { "FailedLoginInterceptor.onFailedLogin($request)" }
        val id = RateLimitUtils.getId(request)
        val tokenBucket: Bucket = resolveBucket(id)
        tokenBucket.tryConsume(1)
        if (tokenBucket.availableTokens == 0L) {
            log.info { "FailedLoginInterceptor: user with IP address $id failed login 5 times." }
        } else {
            log.info { "FailedLoginInterceptor: failed login attempt from $id" }
        }
    }

    // check if this user has exceeded their rate limit
    private fun isAllowed(userId: String): Boolean {
        log.trace { "FailedLoginInterceptor.isAllowed($userId)" }
        val tokenBucket: Bucket = resolveBucket(userId)
        return tokenBucket.availableTokens != 0L
    }


    // get bucket for this user
    private fun resolveBucket(userId: String): Bucket {
        log.trace { "FailedLoginInterceptor.resolveBucket($userId)" }
        return cache.computeIfAbsent(userId) { (this::newBucket)() }
    }

    // create a new bucket with the desired constraints
    // suspend account for 24h if there were 5 failed login attempts
    private fun newBucket(): Bucket {
        log.trace { "FailedLoginInterceptor.newBucket()" }
        return Bucket.builder()
            .addLimit(Bandwidth.classic(allowedAttemptsInDuration, Refill.intervally(allowedAttemptsInDuration, duration)))
            .build()
    }
}
