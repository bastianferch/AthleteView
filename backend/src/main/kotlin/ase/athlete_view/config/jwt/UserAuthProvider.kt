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

import ase.athlete_view.domain.user.pojo.dto.UserDTO
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.*

@Component
@ConfigurationProperties(prefix = "security.jwt.token")
class UserAuthProvider(
    var secretKey: String = ""
) {
    @PostConstruct
    fun init() {
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.toByteArray())
    }

    fun createToken(dto: UserDTO): String {
        val now = Date()
        val validity = Date(now.time + 3600000)
        return JWT.create()
            .withSubject(dto.email)
            .withClaim("id", dto.id)
            .withIssuedAt(now)
            .withExpiresAt(validity)
            .withClaim("name", dto.name)
            .withClaim("userType", dto.userType)
            .sign(Algorithm.HMAC256(secretKey))
    }

    fun validateToken(token: String): Authentication {
        val algorithm = Algorithm.HMAC256(secretKey)
        val verifier = JWT.require(algorithm).build()
        val decoded = verifier.verify(token)
        val user = UserDTO(
            decoded.getClaim("id").asLong(),
            decoded.getClaim("name").asString(),
            decoded.subject,
            null,
            token,
            decoded.getClaim("userType").asString()
        )
        return UsernamePasswordAuthenticationToken(user, null, ArrayList())
    }
}
