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
