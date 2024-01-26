package ase.athlete_view.domain.health.service.impl

import ase.athlete_view.common.exception.entity.InternalException
import ase.athlete_view.domain.health.persistence.HealthRepository
import ase.athlete_view.domain.health.pojo.dto.HealthDTO
import ase.athlete_view.domain.health.pojo.entity.Health
import ase.athlete_view.domain.health.service.HealthService
import ase.athlete_view.domain.user.service.AthleteService
import ase.athlete_view.domain.user.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.http.conn.HttpHostConnectException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Service
class HealthServiceImpl(
    private val healthRepository: HealthRepository,
    private val restTemplate: RestTemplate,
    private val userService: UserService,
    private val athleteService: AthleteService,
) : HealthService {
    val log = KotlinLogging.logger {}

    @Value("\${api.mock.url}")
    val apiHost: String? = null

    override fun syncWithMockServer(userId: Long) {
        log.trace { "S | syncWithMockServer($userId)" }
        val user = this.userService.getById(userId)

        val healthDTOList: List<HealthDTO>
        try {
            healthDTOList = apiHost?.let { this.restTemplate.getForObject(it + "health", Array<HealthDTO>::class.java) }
                ?.toList()
                ?: throw InternalException("Could not access the external API.")
        } catch (e: HttpClientErrorException) {
            throw InternalException("Could not fetch the data from the garmin api.")
        } catch (e: HttpHostConnectException) {
            throw InternalException("Could not establish a connection to the garmin api.")
        }
        val alreadyPersistedList =
            healthRepository.findByDateBeforeAndDateAfter(
                healthDTOList.last().date.plusDays(1),
                healthDTOList[0].date.minusDays(1))
        if (alreadyPersistedList.size == 7) {
            log.debug { "Could not save a syncWithMockServer health, since all data is already persisted." }
            return
        }
        val alreadyPersistedDates = alreadyPersistedList.map { it.date }
        val nonPersistedHealth = healthDTOList
            .filter { health -> health.date !in alreadyPersistedDates }
            .map { healthDTO -> healthDTO.toEntity(user) }

        healthRepository.saveAll(nonPersistedHealth)
    }

    override fun getAllByCurrentUser(userId: Long): List<Health> {
        log.trace { "S | getAllByCurrentUser($userId)" }
        return healthRepository.findByUser_Id(userId)
    }

    override fun getAllFromAthlete(athleteId: Long, userId: Long): List<Health> {
        log.trace { "S | getAllFromAthlete($athleteId, $userId)" }

        val athletes = athleteService.getByTrainerId(userId)
        val athlete = athleteService.getById(athleteId)
        if (userService.getPreferences(athlete.toUserDTO())?.shareHealthWithTrainer == true and
            athletes.contains(athlete)
        ) {
            return healthRepository.findByUser_Id(athleteId)
        }
        return emptyList()
    }

    override fun save(health: Health): Health {
        log.trace { "S | save($health)" }
        return this.healthRepository.save(health)
    }

}