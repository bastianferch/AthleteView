package ase.athlete_view.unit.activity

import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.*
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.util.ActivityCreator
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.UserCreator
import io.github.oshai.kotlinlogging.KotlinLogging
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ActivityServiceUnitTests: TestBase(){
    private val logger = KotlinLogging.logger {}

    @Autowired
    private lateinit var userRepo: UserRepository

    @Autowired
    private lateinit var activityService: ActivityService



    // Create a test object for Step class
    private val step = Step(null, StepType.ACTIVE, StepDurationType.DISTANCE, 30, StepDurationDistanceUnit.KM,
        StepTargetType.CADENCE, 100, 200, "Sample step note")


    // Create a test object for Interval class
    val interval = Interval(null, 1, listOf(Interval(null, 2, listOf(Interval( null, 1, null, step)), null)), null)

    val plannedActivity = PlannedActivity(null, ActivityType.RUN, interval, false, false,
        "Sample planned activity", LocalDateTime.now().plusDays(5), UserCreator.getTrainer(), null,)

    var trainerId: Long = -1L
    var athleteId: Long = -1L

    @BeforeEach
    fun setup() {
        val (tid, aid) = super.createDefaultTrainerAthleteRelationInDb()
        trainerId = tid
        athleteId = aid
    }

    @Test
    fun createValidPlannedActivity_ShouldReturnCategory() {

        // actually does not create from trainer, but from user. if it'd actually user trainer's id, it fails
//        val newPlannedActivity = activityService.createPlannedActivity(plannedActivity,UserCreator.getTrainer().id!!)
        val newPlannedActivity = activityService.createPlannedActivity(plannedActivity, athleteId)
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
        val invalidPlannedActivity = plannedActivity.copy(date= LocalDateTime.now().minusDays(5))
        assertThrows<ValidationException> {
            activityService.createPlannedActivity(invalidPlannedActivity,UserCreator.getTrainer().id!!)
        }
    }

    @Test
    @Disabled
    fun fetchingAllPlannedActivitiesWithoutDates_shouldReturnAllActivities() {
        // setup
        val trainer = UserCreator.getTrainer()
        trainer.id = trainerId

        for (x in 1..5L) {
            val act = ActivityCreator.getDefaultPlannedActivity(trainer, LocalDateTime.now().plusDays(x))
            activityService.createPlannedActivity(act, athleteId)
        }

        // fetch
        val results = activityService.getAllPlannedActivities(athleteId, null, null)
        val tres = activityService.getAllPlannedActivities(trainerId, null, null)

        logger.info { results }
        logger.info { tres }

        // verify
        assertThat(results).isNotNull
        assertThat(results.size).isEqualTo(5)
    }

    @Test
    @Disabled
    fun fetchingAllPlannedActivitiesWithinStartAndEndTime_shouldReturnOnlyMatchingActivities() {
        val storedActivities: MutableList<PlannedActivity> = ArrayList()
        val trainer = UserCreator.getTrainer()
        trainer.id = trainerId

        for (x in 1..10L) {
            val act = ActivityCreator.getDefaultPlannedActivity(trainer, LocalDateTime.now().plusDays(x))
            storedActivities.add(activityService.createPlannedActivity(act, athleteId))
        }

        // fetch
        val now = LocalDateTime.now()
        val endDate = LocalDateTime.now().plusDays(4L)
        val futureResults = activityService.getAllPlannedActivities(athleteId, now, endDate)
        logger.info { "results incl. timerange: $futureResults" }

        // verify
        assertThat(futureResults).isNotNull
        assertThat(futureResults.size).isEqualTo(4)
        assertThat(futureResults).allMatch { now.isBefore(it.date) && endDate.isAfter(it.date) }
    }
}
