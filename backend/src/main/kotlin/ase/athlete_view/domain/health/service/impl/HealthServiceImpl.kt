/*-
 * #%L
 * athlete_view
 * %%
 * Copyright (C) 2023 - 2024 TU Wien INSO ASE GROUP 5 WS2023
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package ase.athlete_view.domain.health.service.impl

import ase.athlete_view.common.exception.entity.InternalException
import ase.athlete_view.domain.health.persistence.HealthRepository
import ase.athlete_view.domain.health.pojo.dto.HealthDTO
import ase.athlete_view.domain.health.pojo.entity.Health
import ase.athlete_view.domain.health.service.HealthService
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.service.AthleteService
import ase.athlete_view.domain.user.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.http.conn.HttpHostConnectException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.time.LocalDate

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

    override fun getAllByUser(userID: Long, start: LocalDate, end: LocalDate): List<Health> {
        log.trace { "S | getAllByUser $userID, start: $start, end: $end" }
        return this.healthRepository.findAllByUserIdAndDateIsAfterAndDateIsBefore(userID, start, end)
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

    override fun createHealthDataForTheLast30Days(athlete: Athlete) {
        val today = LocalDate.now()
        val healthList = mutableListOf<Health>()
        for (i in 1L..30L){
            healthList.add(Health(
                id = null,
                user = athlete,
                date = today.minusDays(i),
                avgSteps = 15000,
                avgBPM = 80,
                avgSleepDuration = 9 * 60
            ))
        }
        this.healthRepository.saveAll(healthList)
    }
}
