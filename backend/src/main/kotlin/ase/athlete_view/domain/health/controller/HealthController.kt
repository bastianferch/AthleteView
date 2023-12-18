package ase.athlete_view.domain.health.controller

import ase.athlete_view.domain.health.service.HealthService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("api/health")
@RestController
class HealthController (private val healthService: HealthService){
    private val logger = KotlinLogging.logger {}

    @PostMapping("/mock")
    fun fillMockData() {
        logger.info { "POST MOCK HEALTH DATA" }
        this.healthService.mock()
    }
}
