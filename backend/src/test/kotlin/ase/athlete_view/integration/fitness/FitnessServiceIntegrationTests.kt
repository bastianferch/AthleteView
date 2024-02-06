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
package ase.athlete_view.integration.fitness

import ase.athlete_view.common.exception.entity.ForbiddenException
import ase.athlete_view.domain.activity.persistence.ActivityRepository
import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.fitness.service.FitnessService
import ase.athlete_view.domain.health.persistence.HealthRepository
import ase.athlete_view.domain.health.pojo.entity.Health
import ase.athlete_view.util.ActivityCreator
import ase.athlete_view.util.HealthCreator
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.UserCreator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class FitnessServiceIntegrationTests : TestBase() {
    @Autowired
    private lateinit var fitnessService: FitnessService

    @Autowired
    private lateinit var activityRepository: ActivityRepository

    @Autowired
    private lateinit var healthRepository: HealthRepository

    @Test
    @DisplayName("(+) Calculate the perfect fitness when only one day is persisted")
    fun calculateFitnessProvidesCorrectData() {
        val user = super.createDefaultUserInDb(UserCreator.DEFAULT_ATHLETE_EMAIL)
        val yesterday = LocalDate.now().minusDays(1)
        val activity = ActivityCreator.getHealthyForDefaultAthleteActivity(user = user, date = yesterday)
        activityRepository.save(activity)

        val health = HealthCreator.getDefaultGoodHealth(
            user = user,
            date = yesterday
        )
        this.healthRepository.save(health)

        assertAll(
            "last week is perfect and one month ago is not available",
            { assertThat(user.id?.let { this.fitnessService.calculate(it, user.id!!).first() }).isEqualTo(100) },
            { assertThat(user.id?.let { this.fitnessService.calculate(it, user.id!!).last() }).isEqualTo(-1) }
        )

    }

    @Test
    @DisplayName("(+) Calculate the perfect fitness when all week is persisted")
    fun calculatePerfectFitnessWhenAllWeakIsPersisted() {
        val user = super.createDefaultUserInDb(UserCreator.DEFAULT_ATHLETE_EMAIL)
        val healthList = ArrayList<Health>()
        val activityList = ArrayList<Activity>()
        val today = LocalDate.now()
        var dayIterator = today.minusDays(7)
        while (!dayIterator.isEqual(today)) {
            val activity = ActivityCreator.getHealthyForDefaultAthleteActivity(user = user, date = dayIterator)
            activityList.add(activity)

            val health = HealthCreator.getDefaultGoodHealth(
                user = user,
                date = dayIterator
            )
            healthList.add(health)

            dayIterator = dayIterator.plusDays(1)
        }
        this.activityRepository.saveAll(activityList)
        this.healthRepository.saveAll(healthList)

        assertThat(user.id?.let { this.fitnessService.calculate(it, user.id!!).first() }).isEqualTo(100)
    }

    @Test
    @DisplayName("(+) Calculate the perfect fitness when two weeks are persisted")
    fun calculatePerfectFitnessWhenTwoWeeksArePersisted() {
        val user = super.createDefaultUserInDb(UserCreator.DEFAULT_ATHLETE_EMAIL)
        val healthList = ArrayList<Health>()
        val activityList = ArrayList<Activity>()
        val today = LocalDate.now()
        var dayIterator = today.minusDays(14)
        val weekAgo = today.minusDays(7)
        while (!dayIterator.isEqual(today)) {
            if (dayIterator.isAfter(weekAgo)) {
                activityList.add(
                    ActivityCreator.getHealthyForDefaultAthleteActivity(user = user, date = dayIterator)
                )
                healthList.add(
                    HealthCreator.getDefaultGoodHealth(
                        user = user,
                        date = dayIterator
                    )
                )
            } else {
                activityList.add(
                    ActivityCreator.getMehForDefaultAthleteActivity(user = user, date = dayIterator)
                )
                healthList.add(
                    HealthCreator.getDefaultMehHealth(
                        user = user,
                        date = dayIterator
                    )
                )
            }
            dayIterator = dayIterator.plusDays(1)
        }
        this.activityRepository.saveAll(activityList)
        this.healthRepository.saveAll(healthList)

        assertThat(user.id?.let { this.fitnessService.calculate(it, user.id!!).first() }).isEqualTo(100)
    }

    @Test
    @DisplayName("(+) Calculate the fitness when only health is persisted")
    fun calculateFitnessWithOnlyHealthPersisted() {
        val user = super.createDefaultUserInDb(UserCreator.DEFAULT_ATHLETE_EMAIL)
        val yesterday = LocalDate.now().minusDays(1)

        val health = HealthCreator.getDefaultGoodHealth(
            user = user,
            date = yesterday
        )
        this.healthRepository.save(health)

        assertThat(user.id?.let { this.fitnessService.calculate(it, user.id!!).first() }).isEqualTo(70)
    }

    @Test
    @DisplayName("(+) Calculate the fitness when only activity is persisted")
    fun calculateFitnessWithOnlyActivityPersisted() {
        val user = super.createDefaultUserInDb(UserCreator.DEFAULT_ATHLETE_EMAIL)
        val yesterday = LocalDate.now().minusDays(1)
        val activity = ActivityCreator.getHealthyForDefaultAthleteActivity(user = user, date = yesterday)
        activityRepository.save(activity)

        assertThat(user.id?.let { this.fitnessService.calculate(it, user.id!!).first() }).isEqualTo(-1)
    }

    @Test
    @DisplayName("(+) Calculate the fitness when only one day is persisted and activity is meh")
    fun calculateFitnessWithLowActivityAndLowHealth() {
        val user = super.createDefaultUserInDb(UserCreator.DEFAULT_ATHLETE_EMAIL)
        val yesterday = LocalDate.now().minusDays(1)
        val activity = ActivityCreator.getDefaultActivity()
        activity.user = user
        activity.startTime = yesterday.atTime(15, 0, 0)
        activity.endTime = yesterday.atTime(16, 0, 0)
        activity.averageBpm = 80
        activity.maxBpm = 110
        activityRepository.save(activity)

        val health = HealthCreator.getDefaultGoodHealth(
            user = user,
            date = yesterday
        )
        this.healthRepository.save(health)

        assertThat(user.id?.let { this.fitnessService.calculate(it, user.id!!).first() }).isEqualTo(94)
    }

    @Test
    @DisplayName("(+) Calculate the fitness when only one day is persisted and there are 2 activities")
    fun calculateFitnessWithOneHealthAndTwoActivitiesAtOneDay() {
        val user = super.createDefaultUserInDb(UserCreator.DEFAULT_ATHLETE_EMAIL)
        val yesterday = LocalDate.now().minusDays(1)
        val activity1 = ActivityCreator.getDefaultActivity()
        activity1.user = user
        activity1.id = null
        activity1.startTime = yesterday.atTime(15, 0, 0)
        activity1.endTime = yesterday.atTime(16, 0, 0)
        activity1.averageBpm = 80
        activity1.maxBpm = 110
        val activity2 = ActivityCreator.getDefaultActivity()
        activity2.user = user
        activity2.id = null
        activity2.startTime = yesterday.atTime(17, 0, 0)
        activity2.endTime = yesterday.atTime(18, 0, 0)
        activity2.averageBpm = 100
        activity2.maxBpm = 190
        activityRepository.saveAll(listOf(activity1, activity2))

        val health = HealthCreator.getDefaultGoodHealth(
            user = user,
            date = yesterday
        )
        this.healthRepository.save(health)

        assertThat(user.id?.let { this.fitnessService.calculate(it, user.id!!).first() }).isEqualTo(94)
    }

    @Test
    @DisplayName("(+) Calculate the fitness when only one day is persisted and both activity and health is meh")
    fun calculateFitnessWithLowActivity() {
        val user = super.createDefaultUserInDb(UserCreator.DEFAULT_ATHLETE_EMAIL)
        val yesterday = LocalDate.now().minusDays(1)
        val activity = ActivityCreator.getDefaultActivity()
        activity.user = user
        activity.startTime = yesterday.atTime(15, 0, 0)
        activity.endTime = yesterday.atTime(16, 0, 0)
        activity.averageBpm = 80
        activity.maxBpm = 110
        activityRepository.save(activity)

        val health = HealthCreator.getDefaultMehHealth(
            user = user,
            date = yesterday
        )
        this.healthRepository.save(health)

        assertThat(user.id?.let { this.fitnessService.calculate(it, user.id!!).first() }).isEqualTo(53)
    }

    @Test
    @DisplayName("(-) Calculate fitness for the trainer")
    fun calculateFitnessForTrainer() {
        val user = super.persistDefaultTrainer(10)
        assertThrows<ForbiddenException> { user.id?.let { this.fitnessService.calculate(it, user.id!!).first() } }
    }

    @Test
    @DisplayName("(+) Calculate fitness when no data for the last week is provided")
    fun calculateFitnessWhenNoDataIsPersisted() {
        val user = super.createDefaultUserInDb(UserCreator.DEFAULT_ATHLETE_EMAIL)
        assertThat(user.id?.let { this.fitnessService.calculate(it, user.id!!).first() }).isEqualTo(-1)
    }

}
