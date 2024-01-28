package ase.athlete_view.config.rate_limit

import ase.athlete_view.common.user.UserUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest

class RateLimitUtils {
    companion object {
        val log = KotlinLogging.logger {}

        fun getId(request: HttpServletRequest): String {
            //if this user is logged in, return the email
            log.trace { "RateLimitUtils.getId($request)" }
            try {
                return UserUtils.currentUser().email
            } catch (_: Exception) {
            }

            // else check if the "X-FORWARDED-FOR" header is present and use that
            val forwardedFor = request.getHeader("X-Forwarded-For")
            if (forwardedFor !== null) {
                return forwardedFor.split(",")[0]
            }

            // else check if the origin header is set
            val origin = request.getHeader("origin")
            if (origin !== null) {
                return origin
            }

            //else return the remote address
            return request.remoteAddr
        }
    }
}