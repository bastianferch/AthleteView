package ase.athlete_view.unit.activity

import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.*
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class ActivityServiceUnitTests {


    @Autowired
    private lateinit var userRepo: UserRepository

    @Autowired
    private lateinit var activityService: ActivityService

    private var athlete = Athlete(
        id = null,
        email = "athlete@example.com",
        name = "Athlete Name",
        password = "athletepassword",
        country = "Athlete Country",
        zip = "54321",
        dob = LocalDate.of(1990, 5, 15), // Date of birth (YYYY, MM, DD)
        height = 175.5, // Height in centimeters or any suitable unit
        weight = 70.5f // Weight in kilograms or any suitable unit
    )

    private var trainer = Trainer(
        id = null,
        email = "test@example.com",
        name = "John Doe",
        password = "secretpassword",
        country = "CountryName",
        zip = "12345"
    )

    // Create a test object for Step class
    private val step = Step(
        id = null,
        type = StepType.ACTIVE,
        durationType = StepDurationType.DISTANCE,
        durationDistance = 30,
        durationDistanceUnit = StepDurationDistanceUnit.KM,
        targetType = StepTargetType.CADENCE,
        targetFrom = 100,
        targetTo = 200,
        note = "Sample step note"
    )


    // Create a test object for Interval class

    val wrapperInterval = Interval(
        id = null,
        repeat = 1,
        intervals = listOf(
            Interval(
                id = null,
                repeat = 2,
                intervals = listOf(
                    Interval(
                        id = null,
                        repeat = 1,
                        intervals = null,
                        step = step
                    )
                ),
                step = null
            )
        ),
        step = null
    )

    // Create a test object for PlannedActivity class
    val plannedActivity = PlannedActivity(
        id = null,
        type = ActivityType.RUN,
        interval = wrapperInterval,
        withTrainer = false,
        template = false,
        note = "Sample planned activity",
        date = LocalDate.now().plusDays(5),
        createdBy = trainer,
        createdFor = null,
    )

    @BeforeEach
    fun setup() {
        athlete = this.userRepo.save(athlete)
        userRepo.save(trainer)
    }
    //should work
    @Test
    fun createValidPlannedActivity_ShouldReturnCategory() {
        val newPlannedActivity = activityService.createPlannedActivity(plannedActivity, trainer.toUserDto())
        assertAll(
            { assert(newPlannedActivity.id != null) },
            { assert(newPlannedActivity.type == plannedActivity.type) },
            { assert(newPlannedActivity.interval == plannedActivity.interval) },
            { assert(newPlannedActivity.withTrainer == plannedActivity.withTrainer) },
            { assert(newPlannedActivity.template == plannedActivity.template) },
            { assert(newPlannedActivity.note == plannedActivity.note) },
            { assert(newPlannedActivity.date == plannedActivity.date) },
            { assert(newPlannedActivity.createdBy == plannedActivity.createdBy) },
            { assert(newPlannedActivity.createdFor == plannedActivity.createdFor) }
        )
    }


}