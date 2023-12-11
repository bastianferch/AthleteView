package ase.athlete_view.domain.health.service.impl

import ase.athlete_view.common.exception.entity.ForbiddenException
import ase.athlete_view.common.user.UserUtils
import ase.athlete_view.domain.health.persistence.HealthRepository
import ase.athlete_view.domain.health.pojo.dto.HealthDTO
import ase.athlete_view.domain.health.pojo.entity.Health
import ase.athlete_view.domain.health.service.HealthService
import ase.athlete_view.domain.user.service.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class HealthServiceImpl(
    private val healthRepository: HealthRepository,
    private val restTemplate: RestTemplate,
    private val userService: UserService
) : HealthService {
    @Value("\${api.mock.url}")
    val apiHost: String? = null
    override fun mock() {
        var answer: HealthDTO? = apiHost?.let { this.restTemplate.getForObject(it, HealthDTO::class.java) }
            ?: throw ForbiddenException("You do not have access for this action.")
        var userDTO = UserUtils.currentUser()
        var user = userDTO.id?.let { this.userService.getById(it) }
            ?: throw ForbiddenException("You do not have access for this action.")
        if (answer != null) {
            this.healthRepository.save(Health(
                id = null,
                user = user,
                avgBPM = answer.avgBPM,
                avgSleepDuration = answer.avgSleepDuration,
                avgSteps = answer.avgSteps,
                date = answer.date
            ))
        }
    }

}