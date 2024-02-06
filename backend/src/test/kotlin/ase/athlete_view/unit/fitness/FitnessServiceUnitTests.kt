package ase.athlete_view.unit.fitness

import ase.athlete_view.common.exception.entity.ForbiddenException
import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.fitness.service.FitnessService
import ase.athlete_view.domain.health.pojo.entity.Health
import ase.athlete_view.domain.health.service.HealthService
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.util.ActivityCreator
import ase.athlete_view.util.HealthCreator
import ase.athlete_view.util.UserCreator
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class FitnessServiceUnitTests {
    @Autowired
    private lateinit var fitnessService: FitnessService

    @MockkBean
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var healthService: HealthService

    @MockkBean
    private lateinit var activityService: ActivityService

    private val userID = 1L;

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


    @BeforeEach
    fun mockData() {
        every { userService.getById(any()) } returns UserCreator.getAthlete(userID)
    }

    @Test
    @DisplayName("(+) Calculate the perfect fitness when only one day is persisted")
    fun calculateFitnessProvidesCorrectData() {
        val yesterday = LocalDate.now().minusDays(1)
        every { healthService.getAllByUser(any(), any(), any()) } returns listOf(
            HealthCreator.getDefaultGoodHealth(UserCreator.getAthlete(1), yesterday)
        )
        every { activityService.getAllActivities(any(), any(), any()) } returns listOf(
            ActivityCreator.getHealthyForDefaultAthleteActivity(UserCreator.getAthlete(1), yesterday)
        )

        Assertions.assertThat(this.fitnessService.calculate(userID, userID).first()).isEqualTo(100)
    }

    @Test
    @DisplayName("(+) Calculate the perfect fitness when all week is persisted")
    fun calculatePerfectFitnessWhenAllWeakIsPersisted() {
        val healthList = ArrayList<Health>()
        val activityList = ArrayList<Activity>()
        val today = LocalDate.now()
        var dayIterator = today.minusDays(7)
        while (!dayIterator.isEqual(today)) {
            val activity = ActivityCreator.getDefaultActivity()
            activity.id = 0
            activity.user = UserCreator.getAthlete(userID)
            activity.startTime = dayIterator.atTime(15, 0, 0)
            activity.endTime = dayIterator.atTime(16, 0, 0)
            activity.averageBpm = 130
            activity.maxBpm = 180
            activityList.add(activity)

            val health = HealthCreator.getDefaultGoodHealth(
                user = UserCreator.getAthlete(userID),
                date = dayIterator
            )
            healthList.add(health)

            dayIterator = dayIterator.plusDays(1)
        }
        every { healthService.getAllByUser(any(), any(), any()) } returns healthList
        every { activityService.getAllActivities(any(), any(), any()) } returns activityList

        Assertions.assertThat(this.fitnessService.calculate(userID, userID).first()).isEqualTo(100)
    }

    @Test
    @DisplayName("(+) Calculate the fitness when only health is persisted")
    fun calculateFitnessWithOnlyHealthPersisted() {
        val yesterday = LocalDate.now().minusDays(1)

        val health = HealthCreator.getDefaultGoodHealth(
            user = UserCreator.getAthlete(userID),
            date = yesterday
        )

        every { healthService.getAllByUser(any(), any(), any()) } returns listOf(health)
        every { activityService.getAllActivities(any(), any(), any()) } returns listOf()

        Assertions.assertThat(this.fitnessService.calculate(userID, userID).first()).isEqualTo(70)
    }

    @Test
    @DisplayName("(+) Calculate the fitness when only activity is persisted")
    fun calculateFitnessWithOnlyActivityPersisted() {
        val yesterday = LocalDate.now().minusDays(1)
        val activity =
            ActivityCreator.getHealthyForDefaultAthleteActivity(user = UserCreator.getAthlete(userID), date = yesterday)
        every { healthService.getAllByUser(any(), any(), any()) } returns listOf()
        every { activityService.getAllActivities(any(), any(), any()) } returns listOf(activity)

        Assertions.assertThat(this.fitnessService.calculate(userID, userID).first()).isEqualTo(-1)
    }


    @Test
    @DisplayName("(+) Calculate the fitness when only one day is persisted and activity is meh")
    fun calculateFitnessWithLowActivityAndLowHealth() {
        val yesterday = LocalDate.now().minusDays(1)
        val activity = ActivityCreator.getDefaultActivity()
        activity.user = UserCreator.getAthlete(userID)
        activity.startTime = yesterday.atTime(15, 0, 0)
        activity.endTime = yesterday.atTime(16, 0, 0)
        activity.averageBpm = 80
        activity.maxBpm = 110

        val health = HealthCreator.getDefaultGoodHealth(
            user = UserCreator.getAthlete(userID),
            date = yesterday
        )
        every { healthService.getAllByUser(any(), any(), any()) } returns listOf(health)
        every { activityService.getAllActivities(any(), any(), any()) } returns listOf(activity)

        Assertions.assertThat(this.fitnessService.calculate(userID, userID).first()).isEqualTo(94)
    }


    @Test
    @DisplayName("(+) Calculate the fitness when only one day is persisted and there are 2 activities")
    fun calculateFitnessWithOneHealthAndTwoActivitiesAtOneDay() {
        val user = UserCreator.getAthlete(userID)
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

        val health = HealthCreator.getDefaultGoodHealth(
            user = user,
            date = yesterday
        )
        every { healthService.getAllByUser(any(), any(), any()) } returns listOf(health)
        every { activityService.getAllActivities(any(), any(), any()) } returns listOf(activity1, activity2)

        Assertions.assertThat(this.fitnessService.calculate(userID, userID).first()).isEqualTo(94)
    }

    @Test
    @DisplayName("(+) Calculate the fitness when only one day is persisted and both activity and health is meh")
    fun calculateFitnessWithLowActivity() {
        val user = UserCreator.getAthlete(userID)
        val yesterday = LocalDate.now().minusDays(1)
        val activity = ActivityCreator.getDefaultActivity()
        activity.user = user
        activity.startTime = yesterday.atTime(15, 0, 0)
        activity.endTime = yesterday.atTime(16, 0, 0)
        activity.averageBpm = 80
        activity.maxBpm = 110

        val health = HealthCreator.getDefaultMehHealth(
            user = user,
            date = yesterday
        )

        every { healthService.getAllByUser(any(), any(), any()) } returns listOf(health)
        every { activityService.getAllActivities(any(), any(), any()) } returns listOf(activity)

        Assertions.assertThat(this.fitnessService.calculate(userID, userID).first()).isEqualTo(53)
    }

    @Test
    @DisplayName("(-) Calculate fitness for the trainer")
    fun calculateFitnessForTrainer() {
        every { userService.getById(any()) } returns UserCreator.getTrainer()
        assertThrows<ForbiddenException> { this.fitnessService.calculate(userID, userID).first() }
    }

    @Test
    @DisplayName("(+) Calculate fitness when no data for the last week is provided")
    fun calculateFitnessWhenNoDataIsPersisted() {
        every { healthService.getAllByUser(any(), any(), any()) } returns listOf()
        every { activityService.getAllActivities(any(), any(), any()) } returns listOf()
        Assertions.assertThat(this.fitnessService.calculate(userID, userID).first()).isEqualTo(-1)
    }

}
