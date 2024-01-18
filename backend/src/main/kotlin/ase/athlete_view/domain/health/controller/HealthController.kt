package ase.athlete_view.domain.health.controller

import ase.athlete_view.domain.health.pojo.dto.HealthDTO
import ase.athlete_view.domain.health.service.HealthService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RequestMapping("api/health")
@RestController
class HealthController (private val healthService: HealthService){
    private val log = KotlinLogging.logger {}

    @PostMapping("/mock")
    fun fillMockData() {
        log.info { "POST fillMockData()" }
        this.healthService.mock()
    }

    // cannot make GET api/health since that would conflict with config kubernetes health endpoint
    @GetMapping("/stats")
    fun getHealth(): HealthDTO {
        log.info { "GET getHealth()" }
        return this.healthService.getAllByCurrentUser().maxByOrNull { health -> health.date }?.toDTO() ?: HealthDTO(null, LocalDate.MIN,-1,-1,-1)
    }

    @GetMapping("/stats/{id}")
    fun getHealthFromAthlete(@PathVariable id: Long): HealthDTO {
        log.info { "GET getHealthFromAthlete($id)" }
        return this.healthService.getAllFromAthlete(id).maxByOrNull { health -> health.date }?.toDTO() ?: HealthDTO(null, LocalDate.MIN,-1,-1,-1)
    }
}
