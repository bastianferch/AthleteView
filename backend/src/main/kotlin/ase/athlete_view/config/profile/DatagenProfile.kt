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
package ase.athlete_view.config.profile

import ase.athlete_view.config.profile.datagen.ActivityDatagen
import ase.athlete_view.domain.activity.persistence.PlannedActivityRepository
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.*
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.health.persistence.HealthRepository
import ase.athlete_view.domain.health.pojo.entity.Health
import ase.athlete_view.domain.health.service.HealthService
import ase.athlete_view.domain.notification.service.NotificationService
import ase.athlete_view.domain.time_constraint.pojo.dto.WeeklyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.entity.TimeFrame
import ase.athlete_view.domain.time_constraint.service.TimeConstraintService
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.domain.zone.service.ZoneService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.serpro69.kfaker.Faker
import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.StringUtils.lowerCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.random.Random

const val NUM_OF_TRAINER = 25 // change this number for acceptance Tests
const val NUM_OF_ATHLETE_PER_TRAINER = 5
const val NUM_OF_ATHLETE_PER_TRAINER_WITH_ACTIVITIES = 3
const val NUM_OF_TRAINER_WITH_ACTIVITIES = 1

@Component
@Profile("datagen")
class DatagenProfile(
    private val userService: UserService,
    private val healthService: HealthService,
    private val healthRepository: HealthRepository,
    private val tcService: TimeConstraintService,
    private val activityService: ActivityService,
    private val plannedActivityRepo: PlannedActivityRepository,
    private val notificationService: NotificationService,
    @Autowired private val mongoTemplate: MongoTemplate,
    private val datagenActivity: ActivityDatagen,
    private val zoneService: ZoneService,
) {


    var log = KotlinLogging.logger {}
    var faker = Faker()
    val nameList = listOf("Boris Rauber", "Holger Weiler", "Elfriede Scheiter", "Moritz Schwanitz", "Julie Hoffmeister", "Patricia Frei")

    @PostConstruct
    fun init() {
        mongoTemplate.dropCollection("fs.files")
        mongoTemplate.dropCollection("fs.chunks")
        log.debug { "Dropped mongodb" }

        val trainer = Trainer(
            1,
            "t@t",
            "trainer",
            BCryptPasswordEncoder().encode("tttttttt"),
            "Austria",
            "1030",
            "ABGVA",
            mutableSetOf(),
            mutableSetOf()
        )
        trainer.isConfirmed = true
        this.userService.save(trainer)
        this.setDefaultAvailability(trainer)
        val athlete = Athlete(
            2,
            "a@a",
            "athlete",
            BCryptPasswordEncoder().encode("aaaaaaaa"),
            "Austria",
            "1050",
            LocalDate.of(2000, 1, 1),
            1800,
            80000,
            trainer,
            null
        )
        athlete.isConfirmed = true
        val saved = this.userService.save(athlete)
        val healthList = ArrayList<Health>()
        val today = LocalDate.now()
        var weekIterator = today.minusDays(7)
        while (!weekIterator.isEqual(today)) {
            healthList.add(
                Health(
                    null,
                    saved,
                    weekIterator,
                    Random.nextInt(5000, 20000),
                    Random.nextInt(50, 110),
                    Random.nextInt(3 * 60, 10 * 60)
                )
            )
            weekIterator = weekIterator.plusDays(1)
        }
        this.healthRepository.saveAll(healthList)

        setDefaultAvailability(athlete)
        this.zoneService.resetZones(saved.id!!)
        this.tcService.createDefaultTimeConstraintsForUser(saved)

        trainer.isConfirmed = true
        athlete.trainer = trainer
        datagenActivity.createPlannedActivities(0, null, trainer)


        val plannedActivity = PlannedActivity(
            null, " 7x(1km P:1min)", ActivityType.RUN,
            Interval(
                null, 1, listOf(
                    Interval(
                        null, 1, null,
                        Step(null, StepType.WARMUP, StepDurationType.LAPBUTTON, null, null, null, null, null, null)
                    ),
                    Interval(
                        null, 7, listOf(
                            Interval(
                                null, 1, null,
                                Step(null, StepType.ACTIVE, StepDurationType.DISTANCE, 1, StepDurationUnit.KM, StepTargetType.PACE, 240, 260, "")
                            ),
                            Interval(
                                null, 1, null,
                                Step(null, StepType.RECOVERY, StepDurationType.TIME, 2, StepDurationUnit.MIN, null, null, null, null)
                            )
                        ),
                        null
                    ),
                    Interval(
                        null, 1, null,
                        Step(null, StepType.COOLDOWN, StepDurationType.LAPBUTTON, null, null, null, null, null, null)
                    )
                ), null
            ),
            false, false, "", LocalDateTime.of(2023, 9, 30, 12, 10), 60, Load.MEDIUM, trainer, athlete, null
        )

        activityService.createInterval(plannedActivity.interval)
        plannedActivityRepo.save(plannedActivity)

        createTrainerAthleteRelations(NUM_OF_TRAINER, NUM_OF_ATHLETE_PER_TRAINER, NUM_OF_ATHLETE_PER_TRAINER_WITH_ACTIVITIES)


    }

    /**
     * creates a number of trainers and athletes
     *
     * @param numTrainer how many trainers should be created
     * @param ratio how many athletes should be created per trainer
     */
    fun createTrainerAthleteRelations(numTrainer: Int, ratio: Int, withActivities: Int) {
        var id = 3L
        var aId = 0
        var tId = 0
        var filesCreated = 0
        var plannedActivitiesCreated = 0
        for (i in 1..numTrainer) {

            val trainer = Trainer(
                id++,
                if (i == 1) "${lowerCase(nameList[0].split(" ")[0])}@${lowerCase(nameList[0].split(" ")[1])}.com" else "t${tId++}@t",
                if (i == 1) nameList[0] else faker.name.name(),
                BCryptPasswordEncoder().encode("tttttttt"),
                faker.address.country(),
                faker.address.postcode(),
                "ABGVA$tId",
                mutableSetOf(),
                mutableSetOf()
            )
            trainer.isConfirmed = true
            this.userService.save(trainer)
            setDefaultAvailability(trainer)
            datagenActivity.createPlannedActivities(0, null, trainer)
            for (j in 1..ratio) {
                val athlete = Athlete(
                    id++,
                    if (i == 1) "${lowerCase(nameList[j].split(" ")[0])}@${lowerCase(nameList[j].split(" ")[1])}.com" else "a${aId++}@a",
                    if (i == 1) nameList[j] else faker.name.name(),
                    BCryptPasswordEncoder().encode("aaaaaaaa"),
                    faker.address.country(),
                    faker.address.postcode(),
                    LocalDate.of(2000, 1, 1),
                    1800,
                    80000,
                    trainer,
                    null
                )
                if (j != ratio) {
                    athlete.isConfirmed = true
                    this.userService.save(athlete)
                    this.zoneService.resetZones(athlete.id!!)
                    trainer.athletes.add(athlete)
                    setDefaultAvailability(athlete)
                    this.userService.saveAll(listOf(trainer, athlete))
                    if (i <= NUM_OF_TRAINER / 2) {
                        val reducePaceInMinKm = faker.random.nextInt(-30, 30)
                        plannedActivitiesCreated += datagenActivity.createPlannedActivities(reducePaceInMinKm, athlete, trainer, j == 1)
                    }

                    if (i <= NUM_OF_TRAINER_WITH_ACTIVITIES) {
                        if (j <= withActivities) {
                            val addSpeedInMS = faker.random.nextFloat() * 1 - 0.5F
                            filesCreated += datagenActivity.changeFiles(addSpeedInMS, faker.random.nextInt(-10, 10), athlete)
                            healthService.createHealthDataForTheLast30Days(athlete)
                        }
                    }
                } else {
                    athlete.isConfirmed = true
                    athlete.trainer = null
                    athlete.trainerToBe = trainer
                    this.userService.save(athlete)
                    this.zoneService.resetZones(athlete.id!!)
                    trainer.unacceptedAthletes += athlete
                    addUnacceptedAthleteNotification(trainer, athlete)
                    this.userService.saveAll(listOf(trainer, athlete))
                    setDefaultAvailability(athlete)

                }
            }
        }
        log.debug { "Created $numTrainer trainers with $ratio athletes each, which leads to a total of ${numTrainer * ratio + numTrainer} users" }
        log.debug { "Created $plannedActivitiesCreated planned activities" }
        log.debug { "Imported $filesCreated activities" }

    }

    fun setDefaultAvailability(user: User) {

        this.tcService.save(
            WeeklyTimeConstraintDto(
                null, true, "JFX Meeting",
                TimeFrame(DayOfWeek.MONDAY, LocalTime.of(19, 0), LocalTime.of(20, 0))
            ),
            UserDTO(user.id, "", "", null, null, "")
        )
        this.tcService.save(
            WeeklyTimeConstraintDto(
                null, false, "Normal Hours",
                TimeFrame(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(20, 0))
            ),
            UserDTO(user.id, "", "", null, null, "")
        )
        this.tcService.save(
            WeeklyTimeConstraintDto(
                null, false, "Normal Hours",
                TimeFrame(DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(20, 0))
            ),
            UserDTO(user.id, "", "", null, null, "")
        )
        this.tcService.save(
            WeeklyTimeConstraintDto(
                null, false, "Normal Hours",
                TimeFrame(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(20, 0))
            ),
            UserDTO(user.id, "", "", null, null, "")
        )
        this.tcService.save(
            WeeklyTimeConstraintDto(
                null, false, "Normal Hours",
                TimeFrame(DayOfWeek.THURSDAY, LocalTime.of(8, 0), LocalTime.of(20, 0))
            ),
            UserDTO(user.id, "", "", null, null, "")
        )
        this.tcService.save(
            WeeklyTimeConstraintDto(
                null, false, "Normal Hours",
                TimeFrame(DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(20, 0))
            ),
            UserDTO(user.id, "", "", null, null, "")
        )
        this.tcService.save(
            WeeklyTimeConstraintDto(
                null, false, "Normal Hours",
                TimeFrame(DayOfWeek.SATURDAY, LocalTime.of(8, 0), LocalTime.of(20, 0))
            ),
            UserDTO(user.id, "", "", null, null, "")
        )
        this.tcService.save(
            WeeklyTimeConstraintDto(
                null, false, "Normal Hours",
                TimeFrame(DayOfWeek.SUNDAY, LocalTime.of(8, 0), LocalTime.of(20, 0))
            ),
            UserDTO(user.id, "", "", null, null, "")
        )
    }

    fun addUnacceptedAthleteNotification(trainer: Trainer, athlete: Athlete) {
        this.notificationService.sendNotification(
            trainer.id!!,
            "Athlete request",
            "Would you like to accept the athlete ${if (athlete.name.length > 40) athlete.name.substring(40) else athlete.name}",
            "action/acceptAthlete/${athlete.id}"
        )
    }
}
