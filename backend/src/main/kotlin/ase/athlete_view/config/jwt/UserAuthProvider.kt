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
