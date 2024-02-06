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

import ase.athlete_view.common.exception.entity.RateLimitExceededException
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class RateLimitInterceptor(private var limit: Long, private var duration: Duration) : HandlerInterceptor {


    val log = KotlinLogging.logger {}

    // cache used for storing user's number of requests
    private val cache: MutableMap<String, Bucket> = ConcurrentHashMap()


    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        log.trace { "RateLimitInterceptor.preHandle($request, $response, $handler)" }
        val userId = RateLimitUtils.getId(request)
        if (isAllowed(userId)) {
            return true
        } else {
            throw RateLimitExceededException("You have exhausted your API Request Limit")
        }
    }

    // check if this user has exceeded their rate limit
    private fun isAllowed(addr: String): Boolean {
        log.trace { "RateLimitInterceptor.isAllowed($addr)" }
        val tokenBucket: Bucket = resolveBucket(addr)
        val probe = tokenBucket.tryConsumeAndReturnRemaining(1)
        return probe.isConsumed
    }

    // get bucket for this user
    private fun resolveBucket(addr: String): Bucket {
        log.trace { "RateLimitInterceptor.resolveBucket($addr)" }
        return cache.computeIfAbsent(addr) { (this::newBucket)() }
    }

    // create a new bucket with the desired constraints
    private fun newBucket(): Bucket {
        log.trace { "RateLimitInterceptor.newBucket()" }
        return Bucket.builder()
            .addLimit(Bandwidth.classic(limit, Refill.intervally(limit, duration)))
            .build()
    }
}
