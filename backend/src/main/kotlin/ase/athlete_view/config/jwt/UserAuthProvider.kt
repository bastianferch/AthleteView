package ase.athlete_view.config.jwt

import ase.athlete_view.domain.user.pojo.dto.UserDto
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

    fun createToken(dto: UserDto): String {
        val now = Date()
        val validity = Date(now.time + 3600000)
        return JWT.create()
            .withSubject(dto.email)
            .withIssuedAt(now)
            .withExpiresAt(validity)
            .withClaim("name", dto.name)
            .sign(Algorithm.HMAC256(secretKey))
    }

    fun validateToken(token: String): Authentication {
        var algorithm = Algorithm.HMAC256(secretKey);
        var verifier = JWT.require(algorithm).build()
        var decoded = verifier.verify(token)
        var user = UserDto(null, decoded.subject, decoded.getClaim("name").asString(), null, token)
        return UsernamePasswordAuthenticationToken(user, null, ArrayList())
    }
}
