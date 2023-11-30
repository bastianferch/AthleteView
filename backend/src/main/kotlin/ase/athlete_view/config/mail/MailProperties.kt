package ase.athlete_view.config.mail

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "spring.mail")
@Component
data class MailProperties (var username: String = ""){

}