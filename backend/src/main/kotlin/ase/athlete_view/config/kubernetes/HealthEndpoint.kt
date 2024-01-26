package ase.athlete_view.config.kubernetes

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RequestMapping("api/health")
@RestController
class HealthEndpoint() {
    private val log = KotlinLogging.logger {}

    private var healthy = true

    @GetMapping
    fun getHealth(): String = if (healthy) {
        "OK"
    } else {
        throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    /**
     * Previously to a shutdown of a pod this url will be called. Afterwards the health probes fail. Therefore the pod
     * is removed from the healthy pods which are exposed. This way a zero downtime upgrade is possible.
     */
    @GetMapping("/prepareShutdown")
    fun preShutdown() {
        log.info { "GET | preShutdown()" }

        healthy = false
        try {
            Thread.sleep(15000)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

}