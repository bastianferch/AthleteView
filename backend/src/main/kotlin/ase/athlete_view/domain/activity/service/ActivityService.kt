package ase.athlete_view.domain.activity.service

import ase.athlete_view.domain.activity.pojo.dto.CommentDTO
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.pojo.entity.Comment
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.user.pojo.entity.User
import com.garmin.fit.FitMessages
import ase.athlete_view.domain.activity.pojo.entity.Step
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

interface ActivityService {
    fun createPlannedActivity(plannedActivity: PlannedActivity, userId: Long, isCsp : Boolean = false): PlannedActivity

    fun getPlannedActivity(id: Long, userId: Long): PlannedActivity

    fun getAllPlannedActivities(userId: Long, startDate: LocalDateTime?, endDate: LocalDateTime?): List<PlannedActivity>

    fun updatePlannedActivity(id: Long, plannedActivity: PlannedActivity, userId: Long): PlannedActivity

    fun importActivity(files: List<MultipartFile>, userId: Long): Activity

    fun getAllActivities(uid: Long, startDate: LocalDateTime?, endDate: LocalDateTime?): List<Activity>

    fun createInterval(interval: Interval): Interval

    fun createStep(step: Step): Step

    fun getAllTemplates(uid:Long):List<PlannedActivity>

    fun calculateStats(data: FitMessages, user: User, item: MultipartFile): Pair<Activity, String>

    fun getSingleActivityForUser(userId: Long, activityId: Long): Activity

    /**
     * stores a comment from the user with id userId for the activity with the specified activityId.
     * Only succeeds if the user has access to this activity (either as athlete or trainer).
     * Text of comment gets sanitized before storing.
     * Comment is validated before storing.
     * A notification is sent to the user who owns this activity (if this user is not the one commenting).
     *
     * @param userId of the user who posts the comment
     * @param activityId of the activity for which the comment is posted
     * @param comment that should be posted
     * @return the sanitized comment object
     */
    fun commentActivityWithUser(userId: Long, activityId: Long, comment: CommentDTO): Comment

    /**
     * stores a rating from the user with id userId for the activity with the specified activityId.
     * Only succeeds if the user has access to this activity (either as athlete or trainer).
     * A notification is sent to the user who owns this activity (if this user is not the one rating).
     *
     * @param userId of the user who posts the rating
     * @param activityId of the rated activity
     * @param rating between 0 and 5
     */
    fun rateActivityWithUser(userId: Long, activityId: Long, rating: Int): Unit

    fun deletePlannedActivities(activities: List<PlannedActivity>)
}
