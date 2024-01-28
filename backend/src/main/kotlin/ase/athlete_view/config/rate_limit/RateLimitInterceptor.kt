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