package ase.athlete_view.domain.health.service.impl

import ase.athlete_view.common.exception.entity.ConflictException
import ase.athlete_view.common.exception.entity.InternalException
import ase.athlete_view.common.user.UserUtils
import ase.athlete_view.domain.health.persistence.HealthRepository
import ase.athlete_view.domain.health.pojo.dto.HealthDTO
import ase.athlete_view.domain.health.service.HealthService
import ase.athlete_view.domain.health.service.mapper.HealthMapper
import ase.athlete_view.domain.user.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class HealthServiceImpl(
    private val healthRepository: HealthRepository,
    private val restTemplate: RestTemplate,
    private val userService: UserService,
    private val healthMapper: HealthMapper
) : HealthService {
    val log = KotlinLogging.logger {}

    @Value("\${api.mock.url}")
    val apiHost: String? = null

    override fun mock() {
        val user = UserUtils.currentUser().id?.let { this.userService.getById(it) }
            ?: throw ConflictException("You do not have access for this action.")
        val healthDTO: HealthDTO = apiHost?.let { this.restTemplate.getForObject(it, HealthDTO::class.java) }
            ?: throw InternalException("Could not access the external API.")
        if (healthRepository.findByDate(healthDTO.date).isPresent){
            log.warn { "Could not save a mock health, since there is an existing Health with date ${healthDTO.date}." }
            return;
        }
        val health = this.healthMapper.toEntity(healthDTO, user)
        this.healthRepository.save(health)
    }

}