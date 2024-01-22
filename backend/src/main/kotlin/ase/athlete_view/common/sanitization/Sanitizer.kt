package ase.athlete_view.common.sanitization

import io.github.oshai.kotlinlogging.KotlinLogging
import org.owasp.html.PolicyFactory
import org.owasp.html.Sanitizers
import org.springframework.stereotype.Service

@Service
class Sanitizer {

    val log = KotlinLogging.logger {}

    fun sanitizeText(text: String): String {
        log.trace { "S | sanitizeText($text)" }
        // only allow formatting tags in header and body
        val policy: PolicyFactory = Sanitizers.FORMATTING
        return policy.sanitize(text)
    }
}