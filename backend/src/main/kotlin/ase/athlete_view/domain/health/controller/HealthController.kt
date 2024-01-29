package ase.athlete_view.domain.health.controller

import ase.athlete_view.domain.health.pojo.dto.HealthDTO
import ase.athlete_view.domain.health.service.HealthService
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RequestMapping("api/health")
@RestController
class HealthController (private val healthService: HealthService){
    private val log = KotlinLogging.logger {}

    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.CREATED)
    fun syncWithMockServer(@AuthenticationPrincipal user: UserDTO) {
        log.info { "POST | syncWithMockServer()" }
        this.healthService.syncWithMockServer(user.id!!)
    }

    // cannot make GET api/health since that would conflict with config kubernetes health endpoint
    @GetMapping("/stats")
    fun getHealth(@AuthenticationPrincipal user: UserDTO): HealthDTO {
        log.info { "GET | getHealth()" }
        return this.healthService.getAllByCurrentUser(user.id!!).maxByOrNull { health -> health.date }?.toDTO() ?: HealthDTO(null, LocalDate.MIN,-1,-1,-1)
    }

    @GetMapping("/stats/{id}")
    fun getHealthFromAthlete(@AuthenticationPrincipal user: UserDTO, @PathVariable id: Long): HealthDTO {
        log.info { "GET | getHealthFromAthlete($id)" }
        return this.healthService.getAllFromAthlete(id, user.id!!).maxByOrNull { health -> health.date }?.toDTO() ?: HealthDTO(null, LocalDate.MIN,-1,-1,-1)
    }
}
