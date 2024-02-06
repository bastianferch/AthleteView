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
package ase.athlete_view.domain.activity.service

import ase.athlete_view.common.exception.entity.InternalException
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.NoMapDataException
import ase.athlete_view.common.exception.fitimport.InvalidFitFileException
import ase.athlete_view.domain.activity.pojo.dto.ActivityStatisticsDTO
import ase.athlete_view.domain.activity.pojo.dto.CommentDTO
import ase.athlete_view.domain.activity.pojo.dto.MapDataDTO
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.pojo.entity.Comment
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.user.pojo.entity.User
import com.garmin.fit.FitMessages
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

interface ActivityService {

    /**
     * creates a new planned activity for the user with the specified userId.
     * The activity is validated before storing.
     *
     * @param plannedActivity that should be stored
     * @param userId of the user who created the activity
     * @param isCsp true if it is created for the csp
     */
    fun createPlannedActivity(plannedActivity: PlannedActivity, userId: Long, isCsp: Boolean = false): PlannedActivity

    /**
     * get the activity with the specified id.
     * Only succeeds if the user has access to this activity (either as athlete or trainer).
     *
     * @param id of the activity that should be loaded
     * @param userId of the user who wants to get the activity
     * @return the planned activity with the specified id
     */
    fun getPlannedActivity(id: Long, userId: Long): PlannedActivity

    /**
     * get all planned activities for the user with the specified userId.
     * Only succeeds if the user has access to this activity (either as athlete or trainer).
     *
     * @param userId of the user who wants to get the activities
     * @param startDate of the interval in which the activities should be loaded
     * @param endDate of the interval in which the activities should be loaded
     * @return list of all planned activities
     */
    fun getAllPlannedActivities(userId: Long, startDate: LocalDateTime?, endDate: LocalDateTime?): List<PlannedActivity>

    /**
     * updates the planned activity with the specified id.
     * Only succeeds if the user has access to this activity (either as athlete or trainer).
     * The activity is validated before storing.
     *
     * @param id of the activity that should be updated
     * @param plannedActivity that should be stored
     * @param userId of the user who wants to update the activity
     * @return the updated planned activity
     */
    fun updatePlannedActivity(id: Long, plannedActivity: PlannedActivity, userId: Long): PlannedActivity

    /**
     * imports the activities from the specified files for the user with the specified userId.
     *
     * @param files that should be imported(only .fit files are supported)
     * @param userId of the user who wants to import the activities
     * @return the last of the imported activities
     */
    fun importActivity(files: List<MultipartFile>, userId: Long): Activity

    /**
     * get all activities for the user with the specified userId.
     *
     * @param uid of the user who wants to get the activities
     * @param startDate of the interval in which the activities should be loaded
     * @param endDate of the interval in which the activities should be loaded
     * @return list of all activities
     */
    fun getAllActivities(uid: Long, startDate: LocalDateTime?, endDate: LocalDateTime?): List<Activity>

    /**
     * Creates a new interval.
     * The interval is validated before storing.
     *
     * @param interval that should be stored
     * @return the created interval
     */
    fun createInterval(interval: Interval): Interval

    /**
     * Creates a new step.
     * The step is validated before storing.
     *
     * @param step that should be stored
     * @return the created step
     */
    fun createStep(step: Step): Step

    /**
     * get all templates for the user with the specified userId.
     *
     * @param uid of the user who wants to get the templates
     * @return list of all templates
     */
    fun getAllTemplates(uid: Long): List<PlannedActivity>

    /**
     * calculates the stats for the specified activity
     *
     * @param data of the activity
     * @param user who owns the activity
     * @param item that was uploaded
     * @return the calculated activity and the hash value of the file
     */
    fun calculateStats(data: FitMessages, user: User, item: MultipartFile): Pair<Activity, String>

    /**
     * get the activity with the specified id.
     * Only succeeds if the user has access to this activity (either as athlete or trainer).
     *
     * @param userId of the user who wants to get the activity
     * @param activityId of the activity that should be loaded
     * @return the activity with the specified id
     */
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

    /**
     * deletes the planned activity with the specified id.
     *
     * @param activities that should be deleted
     */
    fun deletePlannedActivities(activities: List<PlannedActivity>)

    /**
     * Calls the authenticated (see {@link GarminMock}) external API
     * to add a new activity data to the current authenticated user.
     * If the activity data already exists for the given day, does nothing.
     *
     * @param userId id of current authenticated user
     *
     * @throws InternalException when external API did not respond / sent the wrong data.
     * @throws NotFoundException when current authenticated user could not be fetched from db.
     */
    fun syncWithMockServer(userId: Long)

    /**
     * Retrieves map-data (coordinates) for a specified (finished) activity.
     *
     * @param uid id of calling user
     * @param activityId id of (finished) activity to parse
     *
     * @return List of MapDataDTO (latitude-longitude pairs)
     *
     * @throws NoMapDataException in case that no fit-file was imported for the specified activity
     */
    fun prepareMapDataForActivity(uid: Long, activityId: Long): List<MapDataDTO>


    /**
     * Retrieves data from the uploaded activity which is later shown in activity graph(s).
     * Fields parsed include `speed`, `heartRate`, `altitude`, `power` and `cadence`.
     *
     * @param uid the userid of who is requesting this data
     * @param activityId the activity to parse data from
     * @return list of ActivityStatisticsDTO, holding all parsed values rows incl. timestamps
     */
    fun prepareStatisticsForActivity(uid: Long, activityId: Long): List<ActivityStatisticsDTO>
}
