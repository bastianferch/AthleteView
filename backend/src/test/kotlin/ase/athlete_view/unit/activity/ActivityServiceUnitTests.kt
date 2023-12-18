package ase.athlete_view.unit.activity

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.*
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.util.ActivityCreator
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.UserCreator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

import java.time.LocalDateTime

//@SpringBootTest
@ActiveProfiles("test")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ActivityServiceUnitTests: TestBase() {
    @Autowired
    private lateinit var activityService: ActivityService


    // Create a test object for Step class
    private val step = Step(null, StepType.ACTIVE, StepDurationType.DISTANCE, 30, StepDurationUnit.KM,
        StepTargetType.CADENCE, 100, 200, "Sample step note")


    // Create a test object for Interval class
    val interval = Interval(null, 1, listOf(Interval(null, 2, listOf(Interval( null, 1, null, step)), null)), null)

    val plannedActivity = PlannedActivity(null,"test activity", ActivityType.RUN, interval, false, false,
        "Sample planned activity", LocalDateTime.now().plusDays(5), 60, Load.MEDIUM, UserCreator.getTrainer(), null,null)

    lateinit var defaultTrainer: Trainer
    lateinit var defaultAthlete: Athlete

    @BeforeEach
    fun setup() {
        val (athlete, trainer) = super.createDefaultTrainerAthleteRelationInDb()
        defaultTrainer = trainer
        defaultAthlete = athlete
    }

    @Test
    fun createValidPlannedActivity_ShouldReturnCategory() {
        val newPlannedActivity = activityService.createPlannedActivity(plannedActivity, defaultAthlete.id!!)
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
        val date = LocalDateTime.now().minusDays(5)
        val invalidPlannedActivity = ActivityCreator.getDefaultPlannedActivity(defaultTrainer, date, defaultAthlete)
        assertThrows<ValidationException> {
            activityService.createPlannedActivity(invalidPlannedActivity,UserCreator.getTrainer().id!!)
        }
    }

    @Test
    fun fetchingAllPlannedActivitiesForUserWithoutDates_shouldReturnAllActivitiesForTrainerAndUser() {
        // setup
        generateGenericPlannedActivities(5L)

        // fetch
        val athleteActivities = activityService.getAllPlannedActivities(defaultAthlete.id!!, null, null)
        val trainerActivities = activityService.getAllPlannedActivities(defaultTrainer.id!!, null, null)

        // verify
        assertThat(athleteActivities).isNotNull
        assertThat(athleteActivities.size).isEqualTo(5)
        assertThat(trainerActivities).isNotNull
        assertThat(trainerActivities.size).isEqualTo(5)
    }

    @Test
    fun fetchingAllPlannedActivitiesWithinStartAndEndTime_shouldReturnOnlyMatchingActivities() {
        // setup
        generateGenericPlannedActivities(10L)

        // fetch
        val now = LocalDateTime.now()
        val endDate = LocalDateTime.now().plusDays(4L)
        val athleteResults = activityService.getAllPlannedActivities(defaultAthlete.id!!, now, endDate)
        val trainerResults = activityService.getAllPlannedActivities(defaultTrainer.id!!, now, endDate)

        // verify
        assertThat(athleteResults).isNotNull
        assertThat(athleteResults.size).isEqualTo(4)
        assertThat(athleteResults).allMatch { now.isBefore(it.date) && endDate.isAfter(it.date) }

        assertThat(trainerResults).isNotNull
        assertThat(trainerResults.size).isEqualTo(4)
        assertThat(trainerResults).allMatch { now.isBefore(it.date) && endDate.isAfter(it.date) }
    }

    @Test
    fun fetchingAllPlannedActivities_WithPartialAssignedToUser_shouldOnlyReturnThoseForUser() {
        // setup
        // add new user
        val additionalUser = super.createDefaultUserInDb(email = "user@email.at")
        super.addAthleteToTrainer(additionalUser, defaultTrainer)
        // generate activities for default user
        generateGenericPlannedActivities(3L)
        // generate some for additionalUser
        generateGenericPlannedActivities(3L, athlete = additionalUser)

        // fetch
        val results = activityService.getAllPlannedActivities(defaultAthlete.id!!, null, null)

        // verify
        assertThat(results.size).isEqualTo(3)
        assertThat(results).allMatch { it.createdFor == defaultAthlete && it.createdBy == defaultTrainer }
    }

    @Test
    fun fetchSingleActivityForUser_whoHasActivity_shouldReturnActivity() {
        val result = activityService.getSingleActivityForUser(-1, -1)
        assertThat(result).isNotNull
        assertThat(result.user?.id).isEqualTo(-1)
        assertThat(result.id).isEqualTo(-1)
    }

    @Test
    fun fetchSingleActivityForUser_whoHasNoActivity_shouldThrowException() {
        assertThrows<NotFoundException> {
            // no activity with id -2
            activityService.getSingleActivityForUser(-1, -2)
        }

        assertThrows<NotFoundException> {
            // user -2 does not have activity -1
            activityService.getSingleActivityForUser(-2, -1)
        }
    }


    private fun generateGenericPlannedActivities(count: Long = 5L, creator: Long = defaultTrainer.id!!, trainer: Trainer? = null, athlete: Athlete? = null, dateProvided: LocalDateTime? = null) {
        for (x in 1..count) {
            val date = dateProvided ?: LocalDateTime.now().plusDays(x)
            val act = ActivityCreator.getDefaultPlannedActivity(trainer ?: defaultTrainer, date, athlete ?: defaultAthlete)
            activityService.createPlannedActivity(act, creator)
        }
    }
}
