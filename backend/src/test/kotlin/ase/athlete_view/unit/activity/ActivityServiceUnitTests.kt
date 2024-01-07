package ase.athlete_view.unit.activity

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.activity.pojo.dto.CommentDTO
import ase.athlete_view.domain.activity.pojo.entity.Comment
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.*
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.notification.service.NotificationService
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.util.ActivityCreator
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.UserCreator
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
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

    @MockkBean
    private lateinit var notificationService: NotificationService


    // Create a test object for Step class
    private val step = Step(null, StepType.ACTIVE, StepDurationType.DISTANCE, 30, StepDurationUnit.KM,
        StepTargetType.CADENCE, 100, 200, "Sample step note")


    // Create a test object for Interval class
    val interval = Interval(null, 1, listOf(Interval(null, 2, listOf(Interval( null, 1, null, step)), null)), null)

    val plannedActivity = PlannedActivity(null,"test activity", ActivityType.RUN, interval, false, false,
        "Sample planned activity", LocalDateTime.now().plusDays(5), 60, Load.MEDIUM, UserCreator.getTrainer(), null,null)

    lateinit var defaultTrainer: Trainer
    lateinit var defaultAthlete: Athlete

    val commentDto: CommentDTO = CommentDTO(-1, "test comment")
    val commentDtoTooLong: CommentDTO = CommentDTO(-1, "too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too long too lo")

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
            activityService.createPlannedActivity(invalidPlannedActivity,defaultTrainer.id!!)
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
        assertAll(
            { assertThat(athleteActivities).isNotNull },
            { assertThat(athleteActivities.size).isEqualTo(5) },
            { assertThat(trainerActivities).isNotNull },
            { assertThat(trainerActivities.size).isEqualTo(5) },
        )
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
        assertAll(
            { assertThat(athleteResults).isNotNull },
            { assertThat(athleteResults.size).isEqualTo(4) },
            { assertThat(athleteResults).allMatch { now.isBefore(it.date) && endDate.isAfter(it.date) } },
            { assertThat(trainerResults).isNotNull },
            { assertThat(trainerResults.size).isEqualTo(4) },
            { assertThat(trainerResults).allMatch { now.isBefore(it.date) && endDate.isAfter(it.date) } },
        )
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
        assertAll(
            { assertThat(results.size).isEqualTo(3) },
            { assertThat(results).allMatch { it.createdFor == defaultAthlete && it.createdBy == defaultTrainer } },
        )
    }

    @Test
    fun fetchSingleActivityForUser_whoHasActivity_shouldReturnActivity() {
        val result = activityService.getSingleActivityForUser(-1, -1)
        assertAll(
            { assertThat(result).isNotNull },
            { assertThat(result.user?.id).isEqualTo(-1) },
            { assertThat(result.id).isEqualTo(-1) },
        )

    }

    @Test
    fun fetchSingleActivityForUser_whoHasNoActivity_shouldThrowException() {

        assertAll(
            { assertThrows<NotFoundException> {
                    // no activity with id -2
                    activityService.getSingleActivityForUser(-1, -2)
                } },

            { assertThrows<NotFoundException> {
                    // user -2 does not have activity -1
                    activityService.getSingleActivityForUser(-2, -1)
                } },
        )
    }


    // COMMENTS

    @Test
    fun commentOnNonExistentActivity_shouldThrowException() {
        every { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) } returns null;
        assertThrows<NotFoundException> { activityService.commentActivityWithUser(-1, 9999, commentDto) }
        verify(exactly = 0) { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) }
    }

    @Test
    fun commentOnActivityWithUserWhoHasNoAccessToActivity_shouldThrowNotFound() {
        every { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) } returns null;
        // -2 has no access to activity -1
        assertThrows<NotFoundException> { activityService.commentActivityWithUser(-2, -1, commentDto) }
        verify(exactly = 0) { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) }
    }

    @Test
    fun commentOnActivityWithUserWhoOwnsActivity_shouldStoreCommentAndSendNotificationToTrainer() {
        every { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) } returns null;
        // -2 has access to activity -2
        var commentResult: Comment? = null
        assertDoesNotThrow { commentResult = activityService.commentActivityWithUser(-2, -2, commentDto) }

        assertAll(
            { assert(commentResult != null) },
            { assert(commentResult!!.text == commentDto.text) },
            { assert(commentResult!!.author!!.id == -2L) },
        )

        // should send notification to trainer because -2 owns the activity and -3 is the trainer of -2
        verify(exactly = 1) { notificationService.sendNotification(-3, any(), any(), any(), any()) }
    }

    @Test
    fun commentOnActivityWithTrainerOfAthleteWhoOwnsActivity_shouldStoreCommentAndSendNotificationToAthlete() {
        every { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) } returns null;
        // -3 has access to activity -2
        var commentResult: Comment? = null
        assertAll(
            { assertDoesNotThrow { commentResult = activityService.commentActivityWithUser(-3, -2, commentDto) } },
            { assert(commentResult != null) },
            { assert(commentResult!!.text == commentDto.text) },
            { assert(commentResult!!.author!!.id == -3L) },
        )

        // should send notification to athlete -2 because -2 owns the activity and -3 is the trainer of -2
        verify(exactly = 1) { notificationService.sendNotification(-2, any(), any(), any(), any()) }
    }

    @Test
    fun commentOnActivityWithTrainerWhoHasAccessToActivity_shouldStoreCommentAndSendNotification() {
        every { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) } returns null;
        // -3 is trainer of -2, who owns activity -2
        var commentResult: Comment? = null
        assertAll(
            { assertDoesNotThrow { commentResult = activityService.commentActivityWithUser(-3, -2, commentDto) } },
            { assert(commentResult != null) },
            { assert(commentResult!!.text == commentDto.text) },
            { assert(commentResult!!.author!!.id == -3L) },
        )

        // should send notification
        verify(exactly = 1) { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) }
    }

    @Test
    fun commentOnActivityWithTooLongComment_shouldThrowValidationException() {
        every { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) } returns null;
        // -1 has access to activity -1
        assertThrows<ValidationException> { activityService.commentActivityWithUser(-1, -1, commentDtoTooLong) }
        verify(exactly = 0) { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) }
    }


    // RATINGS

    @Test
    fun rateNonExistentActivity_shouldThrowException() {
        every { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) } returns null;
        assertThrows<NotFoundException> { activityService.rateActivityWithUser(-1, 9999, 0) }
        verify(exactly = 0) { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) }
    }

    @Test
    fun rateActivityWithUserWhoHasNoAccessToActivity_shouldThrowNotFound() {
        every { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) } returns null;
        // -2 has no access to activity -1
        assertThrows<NotFoundException> { activityService.rateActivityWithUser(-2, -1, 0) }
        verify(exactly = 0) { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) }
    }

    @Test
    fun rateActivityWithUserWhoOwnsActivity_shouldSendNotificationToTrainer() {
        every { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) } returns null;
        // -2 has access to activity -2
        assertDoesNotThrow { activityService.rateActivityWithUser(-2, -2, 0) }
        // should not send notification because -2 owns the activity and also comments on it
        verify(exactly = 1) { notificationService.sendNotification(-3, any(), any(), any(), any()) }
    }

    @Test
    fun rateActivityWithTrainerOfUserWhoOwnsActivity_shouldSendNotificationToUser() {
        every { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) } returns null;
        // -3 has access to activity -2
        assertDoesNotThrow { activityService.rateActivityWithUser(-3, -2, 0) }
        // should not send notification because -2 owns the activity their trainer -3 comments on it
        verify(exactly = 1) { notificationService.sendNotification(-2, any(), any(), any(), any()) }
    }

    @Test
    fun rateActivityWithTrainerWhoHasAccessToActivity_shouldSendNotification() {
        every { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) } returns null;
        // -3 is trainer of -2, who owns activity -2
        assertDoesNotThrow { activityService.rateActivityWithUser(-3, -2, 0) }
        // should send notification
        verify(exactly = 1) { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) }
    }

    @Test
    fun rateActivityWithInvalidRating_shouldThrowValidationException() {
        every { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) } returns null;
        // -1 has access to activity -1
        assertAll(
            { assertThrows<ValidationException> { activityService.rateActivityWithUser(-1, -1, 10) } },
            { assertThrows<ValidationException> { activityService.rateActivityWithUser(-1, -1, -1) } },
        )
        verify(exactly = 0) { notificationService.sendNotification(any<Long>(), any(), any(), any(), any()) }
    }

    private fun generateGenericPlannedActivities(count: Long = 5L, creator: Long = defaultTrainer.id!!, trainer: Trainer? = null, athlete: Athlete? = null, dateProvided: LocalDateTime? = null) {
        for (x in 1..count) {
            val date = dateProvided ?: LocalDateTime.now().plusDays(x)
            val act = ActivityCreator.getDefaultPlannedActivity(trainer ?: defaultTrainer, date, athlete ?: defaultAthlete)
            activityService.createPlannedActivity(act, creator)
        }
    }
}
