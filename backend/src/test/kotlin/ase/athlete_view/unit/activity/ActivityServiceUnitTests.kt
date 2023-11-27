package ase.athlete_view.unit.activity

import ase.athlete_view.common.exception.entity.ValidationException
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
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ActivityServiceUnitTests {


    @Autowired
    private lateinit var userRepo: UserRepository

    @Autowired
    private lateinit var activityService: ActivityService

    private var athlete = Athlete(
        null, "athlete@example.com", "Athlete Name", "athletepassword",
        "Athlete Country", "54321", LocalDate.of(1990, 5, 15), 175.5, 70.5f
    )

    private var trainer = Trainer(null, "test@example.com", "John Doe", "secretpassword", "CountryName", "12345")

    // Create a test object for Step class
    private val step = Step(null, StepType.ACTIVE, StepDurationType.DISTANCE, 30, StepDurationDistanceUnit.KM,
        StepTargetType.CADENCE, 100, 200, "Sample step note")


    // Create a test object for Interval class
    val interval = Interval(null, 1, listOf(Interval(null, 2, listOf(Interval( null, 1, null, step)), null)), null)

    val plannedActivity = PlannedActivity(null, ActivityType.RUN, interval, false, false,
        "Sample planned activity", LocalDate.now().plusDays(5), trainer, null,)

    @BeforeEach
    fun setup() {
        athlete = userRepo.save(athlete)
        trainer = userRepo.save(trainer)
    }

    @Test
    fun createValidPlannedActivity_ShouldReturnCategory() {
        val newPlannedActivity = activityService.createPlannedActivity(plannedActivity)
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

    @Test
    fun createPlannedActivityWithInvalidType_ShouldThrowValidationException() {
        val invalidPlannedActivity = plannedActivity.copy(date= LocalDate.now().minusDays(5))
        assertThrows<ValidationException> {
            activityService.createPlannedActivity(invalidPlannedActivity)
        }

    }



}