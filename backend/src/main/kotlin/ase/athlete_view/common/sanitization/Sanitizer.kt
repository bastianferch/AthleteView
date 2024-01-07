package ase.athlete_view.common.sanitization

import org.owasp.html.PolicyFactory
import org.owasp.html.Sanitizers
import org.springframework.stereotype.Service

@Service
class Sanitizer {

    fun sanitizeText(text: String): String {
        // only allow formatting tags in header and body
        val policy: PolicyFactory = Sanitizers.FORMATTING;
        return policy.sanitize(text)
    }
}