package ase.athlete_view.domain.activity.service.impl

import  ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.sanitization.Sanitizer
import ase.athlete_view.domain.activity.persistence.*
import ase.athlete_view.domain.activity.pojo.dto.CommentDTO
import ase.athlete_view.domain.activity.pojo.entity.*
import ase.athlete_view.domain.activity.pojo.util.*
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.activity.service.validator.ActivityValidator
import ase.athlete_view.domain.activity.util.FitParser
import ase.athlete_view.domain.activity.util.TimeDateUtil
import ase.athlete_view.domain.notification.pojo.entity.NotificationType
import ase.athlete_view.domain.notification.service.NotificationService
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import com.garmin.fit.FitMessages
import com.garmin.fit.Intensity
import com.garmin.fit.LapMesg
import com.garmin.fit.LapTrigger
import com.garmin.fit.RecordMesg
import com.garmin.fit.Sport
import ase.athlete_view.domain.activity.pojo.util.ActivityType as MyActivityType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Service
class ActivityServiceImpl(
    private val plannedActivityRepo: PlannedActivityRepository,
    private val lapRepo: LapRepository,
    private val intervalRepo: IntervalRepository,
    private val stepRepo: StepRepository,
    private val userRepository: UserRepository,
    private val validator: ActivityValidator,
    private val fitParser: FitParser,
    private val activityRepo: ActivityRepository,
    private val fitFileRepo: FitDataRepositoryImpl,
    private val sanitizer: Sanitizer,
    private val notificationService: NotificationService,
    private val timeDateUtil: TimeDateUtil
) : ActivityService {
    private val log = KotlinLogging.logger {}

    @Transactional
    override fun createPlannedActivity(plannedActivity: PlannedActivity, userId: Long, isCsp: Boolean): PlannedActivity {
        log.trace { "S | createPlannedActivity($plannedActivity, $userId, $isCsp)" }

        // get the logged-in user
        val user = userRepository.findById(userId)
        if (!user.isPresent) {
            throw BadCredentialsException("User not found")
        }

        // activity is always created by the logged-in user
        val usr = user.get()
        validator.validateNewPlannedActivity(plannedActivity, usr, isCsp)
        calculateTimeAndLoad(plannedActivity)

        plannedActivity.createdBy = usr

        // verify if `createdFor` actually exists?
        if (plannedActivity.createdFor != null) {
            val forUser = userRepository.findById(plannedActivity.createdFor?.id!!)
            if (!forUser.isPresent) {
                throw NotFoundException("Cannot create activity for unknown user!")
            } else if (forUser.get() !is Athlete) {
                // TODO: handle properly ig
                throw RuntimeException("Something's off...")
            }

            plannedActivity.createdFor = forUser.get() as Athlete
        }

        createInterval(plannedActivity.interval)
        return this.plannedActivityRepo.save(plannedActivity)
    }

    @Transactional
    override fun getPlannedActivity(id: Long, userId: Long): PlannedActivity {
        log.trace { "S | getPlannedActivity ($id, $userId)" }

        // activity is fetched right away, so we don't have to do unnecessary computation for nonexistent activities
        val activity = this.plannedActivityRepo.findById(id).orElseThrow { NotFoundException("Planned Activity not found") }

        // get the logged-in user
        val user = userRepository.findById(userId)
        if (!user.isPresent) {
            throw BadCredentialsException("User not found")
        }

        val userObject = user.get()

        // Athletes can only see their own activities
        if (userObject is Athlete) {
            val activitiesForUser = plannedActivityRepo.findAllByCreatedForId(userObject.id!!)
            val activities = userObject.activities + activitiesForUser
            if (activities.none { it.id == id }) {
                throw NotFoundException("Planned Activity not found")
            }
        } else if (userObject is Trainer) {
            // Trainers see activities of their Athletes and their own templates
            val isOwnTemplate = userObject.activities.any { it.id == id }
            var isForAthleteOfTrainer = false
            for (athlete in userObject.athletes) {
                if (athlete.activities.any { it.id == id }) {
                    isForAthleteOfTrainer = true
                }
            }
            if (!isOwnTemplate && !isForAthleteOfTrainer) {
                throw NotFoundException("Planned Activity not found")
            }
        }

        // if all checks pass, return the activity
        return activity
    }


    override fun getAllPlannedActivities(userId: Long, startDate: LocalDateTime?, endDate: LocalDateTime?): List<PlannedActivity> {
        log.trace { "S | getAllPlannedActivities($userId, $startDate, $endDate)" }

        // get the logged-in user
        val user = userRepository.findById(userId)
        if (!user.isPresent) {
            throw BadCredentialsException("User not found!")
        }

        val userObject = user.get()
        var activities: Set<PlannedActivity> = userObject.activities.toMutableSet()

        // Athletes can only see their own activities
        if (userObject is Athlete) {
            activities = userObject.activities

            // athletes can also see activities `createdFor` them
            val elems = plannedActivityRepo.findAllByCreatedForId(userId)
            activities.addAll(elems)
        } else if (userObject is Trainer) {
            // Trainers see activities of their Athletes and their own templates
            activities = userObject.activities
            for (athlete in userObject.athletes) {
                activities = activities + athlete.activities
            }
        }

        if (startDate != null) {
            activities = activities.filter { it.date != null && startDate.isBefore(it.date) }.toSet()
        }

        if (endDate != null) {
            activities = activities.filter { it.date != null && endDate.isAfter(it.date) }.toSet()
        }

        return activities.toList()
    }

    override fun getAllTemplates(uid: Long): List<PlannedActivity> {
        log.trace { "S | getAllTemplates($uid)" }
        return plannedActivityRepo.findAllTemplatesForUid(uid)
    }

    @Transactional
    override fun updatePlannedActivity(id: Long, plannedActivity: PlannedActivity, userId: Long): PlannedActivity {
        log.trace { "S | updatePlannedActivity($id, $plannedActivity, $userId)" }

        // get the logged-in user
        val user = userRepository.findById(userId)
        if (!user.isPresent) {
            throw BadCredentialsException("User not found!")
        }
        plannedActivity.createdBy = user.get()

        // get the original activity
        val oldPlannedActivity = this.plannedActivityRepo.findById(id).orElseThrow { NotFoundException("Planned Activity not found") }

        // check if the user can edit this activity and if the new one is valid
        validator.validateEditPlannedActivity(plannedActivity, oldPlannedActivity, user.get())
        val result = calculateTimeAndLoad(plannedActivity)
        plannedActivity.load = result.load
        plannedActivity.estimatedDuration = result.estimatedDuration
        plannedActivity.interval = createInterval(plannedActivity.interval)
        return this.plannedActivityRepo.save(plannedActivity)
    }

    @Transactional
    override fun importActivity(files: List<MultipartFile>, userId: Long): Activity {
        log.trace { "S | importActivity($userId)" }
        log.debug { "Ready to parse ${files.size} (${files[0].name}) files for user w/ ID $userId" }

        val user = userRepository.findById(userId)
        if (!user.isPresent) {
            throw BadCredentialsException("User not found")
        }

        var ids = arrayOf<String>().toMutableList()
        var respData: Activity? = null
        for (item in files) {
            val data = fitParser.decode(item.inputStream)

            if (data.recordMesgs.size == 0) {
                continue // skip empty file
            }

            val result = calculateStats(data, user.get(), item)
            respData = result.first
            ids.add(result.second)


        }
        if (respData != null) {
            return respData
        } else {
            throw NotImplementedError("Health fit files are currently not supported")
        }
    }

    @Transactional
    override fun deletePlannedActivities(activities: List<PlannedActivity>) {
        log.trace { "S | deleteActivities($activities)" }
        for (elem in activities) {
            plannedActivityRepo.delete(elem)
        }
    }

    @Transactional
    override fun calculateStats(data: FitMessages, user: User, item: MultipartFile): Pair<Activity, String> {
        log.trace { "S | calculateStats($user)" }
        var powerSum = 0
        var hrSum = 0
        var calSum = 0
        var cadenceSum = 0
        var hrMax: Short = 0
        var hrMin: Short = Short.MAX_VALUE
        var powerMax = 0
        var accuracySum = 0
        var intensityValueMissing = 0

        val lapList = data.lapMesgs
        var i = 0
        var j = 0
        var lap = lapList[i]
        var lastLap = lapList[0]
        val laps: MutableList<Lap> = mutableListOf()
        var curLapIntensity: Intensity? = lap.intensity

        // add first lap
        laps.add(
            Lap(
                null,
                i,
                lap.totalTimerTime.toInt(),
                lap.totalDistance?.toInt(),
                lap.enhancedAvgSpeed,
                lap.avgPower?.toInt(),
                lap.maxPower?.toInt(),
                lap.avgHeartRate?.toInt(),
                lap.maxHeartRate?.toInt(),
                lap.avgCadence?.toInt(),
                lap.maxCadence?.toInt(),
                mapFitIntensityToStepType(lap.intensity)
            )
        )
        var compare = data.sessionMesgs[0].sport != null
        var sameStructure = false
        var sameDurations = false
        var stepList: List<Step>? = null
        var plannedActivity: PlannedActivity? = null
        val fitSportType = if (data.sessionMesgs.size > 0) data.sessionMesgs[0].sport else Sport.GENERIC

        /*
         * if the activity type is set, we can compare the activity to the planned activity
         * this happens in two steps:
         * 1. check if the structure of the activity matches the structure of the planned activity
         * 2. check if the durations of the laps match the durations of the steps
         */
        if (compare) {


            val startTime = timeDateUtil.convertToLocalDateTime(data.recordMesgs[0].timestamp.timestamp)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
            val endTime = timeDateUtil.convertToLocalDateTime(data.recordMesgs[0].timestamp.timestamp)
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(0)
            val activityType = mapFitActivityTypeToActivityType(fitSportType)

            val plannedActivityList = getPlannedActivityByTypeUserIdAndDate(user.id!!, activityType, startTime, endTime)
            plannedActivity = if (plannedActivityList.isNotEmpty()) plannedActivityList[0] else null
            stepList = plannedActivity?.unroll()


            compare = !stepList.isNullOrEmpty()

            if (compare) {
                sameStructure = compareLapLists(stepList!!, lapList)
                if (!sameStructure) {
                    sameDurations = compareLapDurations(stepList, lapList)
                }
            }
        }


        var startTime: LocalDateTime? = null
        var endTime: LocalDateTime? = null

        var recordMsgHolder: RecordMesg? = null

        /*
        * go through all record messages and calculate the stats
        *
         */
        for (d in data.recordMesgs) {
            if (startTime === null) {
                startTime = timeDateUtil.convertToLocalDateTime(d.timestamp!!.timestamp)
            }

            recordMsgHolder = d
            // add every lap to the list for later saving in the repo
            if (lap.timestamp < d.timestamp && i + 1 < lapList.size) {
                lastLap = lap
                lap = lapList[++i]

                laps.add(
                    Lap(
                        null, i, lap.totalTimerTime.toInt(), lap.totalDistance?.toInt(), lap.enhancedAvgSpeed, lap.avgPower?.toInt(),
                        lap.maxPower?.toInt(), lap.avgHeartRate?.toInt(), lap.maxHeartRate?.toInt(), lap.avgCadence?.toInt(), lap.maxCadence?.toInt(),
                        mapFitIntensityToStepType(lap.intensity)
                    )
                )
            }

            /*
             * if the activity type is set, we can compare the activity to the planned activity
             * if structure or durations match, we use this to go to the next rounds
             * in the next if block any datapoint inside the planned activity is added to the accuracy sum if it is in the correct range
             */
            if (compare) {
                // get next lap if the intensity changes
                if (sameStructure) {
                    if (curLapIntensity != lap.intensity) {
                        curLapIntensity = lap.intensity
                        j++
                    }
                } else if (sameDurations) {
                    if (lastLap.lapTrigger == stepList!![j].durationType) {
                        j++
                    }
                }

                // get target type and check if the value is in the range
                if (stepList!![j].targetType == StepTargetType.CADENCE) {
                    if (d.cadence == null) {
                        intensityValueMissing++
                    } else {
                        accuracySum += isBetween(d.cadence.toInt(), stepList[j].targetFrom ?: 0, stepList[j].targetTo ?: 0)
                    }
                } else if (stepList[j].targetType == StepTargetType.HEARTRATE) {
                    if (d.heartRate == null) {
                        intensityValueMissing++
                    } else {
                        accuracySum += isBetween(d.heartRate.toInt(), stepList[j].targetFrom ?: 0, stepList[j].targetTo ?: 0)
                    }
                } else if (stepList[j].targetType == StepTargetType.PACE) {
                    if (d.enhancedSpeed == null) {
                        intensityValueMissing++
                    } else {
                        accuracySum += isBetween(
                            convertMetersPerSecondToSecondsPerKilometer(d.enhancedSpeed),
                            stepList[j].targetFrom ?: 0,
                            stepList[j].targetTo ?: 0
                        )
                    }
                } else if (stepList[j].targetType == StepTargetType.SPEED) {
                    if (d.enhancedSpeed == null) {
                        intensityValueMissing++
                    } else {
                        accuracySum += isBetween(
                            convertMetersPerSecondToKilometerPerHour(d.enhancedSpeed),
                            stepList[j].targetFrom ?: 0,
                            stepList[j].targetTo ?: 0
                        )
                    }
                } else {
                    intensityValueMissing++
                }
            }

            val hr = d.heartRate ?: 0
            val power = d.power ?: 0
            val cal = d.calories ?: 0
            val cadence = d.cadence ?: 0

            if (hr > hrMax) {
                hrMax = hr
            }

            if (hr < hrMin) {
                hrMin = hr
            }

            if (power > powerMax) {
                powerMax = power
            }

            calSum += cal
            powerSum += power
            hrSum += hr
            cadenceSum += cadence
        }

        // final calculation steps
        endTime = timeDateUtil.convertToLocalDateTime(recordMsgHolder!!.timestamp!!.timestamp)

        val totalElems = data.recordMesgs.size
        val avgBpm = hrSum / totalElems
        val avgPower = powerSum / totalElems
        val avgCadence = cadenceSum / totalElems
        val accuracy = ((accuracySum.toFloat() / (totalElems - intensityValueMissing)) * 100).toInt()
        val newDistance = laps.map { it.distance ?: 0 }.reduce { acc, i -> acc + i }
        val totalDistance = newDistance.toDouble()

        // if accuracy too low do not count as planned activity

        plannedActivity = if (compare && accuracy < 25) null else plannedActivity

        val fitId: String = fitFileRepo.saveFitData(item.inputStream, item.name)

        val activity = Activity(
            null,
            user,
            accuracy,
            avgBpm,
            hrMin.toInt(),
            hrMax.toInt(),
            totalDistance,
            calSum,
            avgCadence,
            avgPower,
            powerMax,
            fitId,
            startTime,
            endTime,
            plannedActivity,
            laps,
            mapFitActivityTypeToActivityType(fitSportType)
        )
        plannedActivity?.activity = activity
        laps.map { lapRepo.save(it) }

        return activityRepo.save(activity) to fitId
    }

    private fun compareLapLists(stepList: List<Step>, lapList: List<LapMesg>): Boolean {
        log.trace { "S | compareLapLists($stepList, $lapList)" }
        var i = 0
        for (lap in lapList) { // go through all laps
            val stepIntensity = stepList[i].type
            val lapIntensity = mapFitIntensityToStepType(lap.intensity)
            if (lapIntensity != stepIntensity) {
                if (i == stepList.size - 1) { // all steps are done and more laps
                    return false
                } else if (lapIntensity != stepList[i + 1].type) { // next step is also not the correct one
                    return false
                } else {
                    i++
                }
            }
        }
        return true
    }


    override fun getAllActivities(uid: Long, startDate: LocalDateTime?, endDate: LocalDateTime?): List<Activity> {
        log.trace { "S | getAllActivities($uid, $startDate, $endDate)" }
        val userObject = userRepository.findById(uid).getOrNull()
            ?: throw NotFoundException("No such user!")

        var activities: MutableSet<Activity> = mutableSetOf()

        // Athletes can only see their own activities
        if (userObject is Athlete) {
            if (startDate != null && endDate != null) {
                activities = activityRepo.findActivitiesByUserAndDateRange(userObject.id!!, startDate, endDate).toMutableSet()
            } else {
                activities = activityRepo.findActivitiesByUserId(uid).toMutableSet()
            }
        } else if (userObject is Trainer) {
            // Trainers see activities of their Athletes
            for (athlete in userObject.athletes) {
                if (startDate != null && endDate != null) {
                    activities += activityRepo.findActivitiesByUserAndDateRange(athlete.id!!, startDate, endDate).toSet()
                } else {
                    activities += activityRepo.findActivitiesByUserId(athlete.id!!).toSet()
                }
            }
        }
        return activities.toList()
    }

    private fun compareLapDurations(stepList: List<Step>, lapList: List<LapMesg>): Boolean {
        log.trace { "S | compareLapDurations($stepList, $lapList)" }
        var i = 0
        for (step in stepList) { // go through all steps
            when (step.durationType) {
                StepDurationType.LAPBUTTON -> {
                    while (lapList[i].lapTrigger != LapTrigger.MANUAL) {
                        if (i + 1 == lapList.size) { // all laps are done and more steps
                            return false
                        }
                        i++
                    }
                }

                StepDurationType.TIME -> {
                    while (lapList[i].lapTrigger != LapTrigger.TIME) {
                        if (i + 1 == lapList.size) { // all laps are done and more steps
                            return false
                        }
                        i++
                    }
                }

                StepDurationType.DISTANCE -> {
                    while (lapList[i].lapTrigger != LapTrigger.DISTANCE) {
                        if (i + 1 == lapList.size) { // all laps are done and more steps
                            return false
                        }
                        i++
                    }
                }

                null -> return false
            }
        }
        return true
    }

    private fun getPlannedActivityByTypeUserIdAndDate(id: Long, type: ActivityType, startTime: LocalDateTime, endTime: LocalDateTime): List<PlannedActivity> {
        log.trace { "S | getPlannedActivityByTypeUserIdAndDate($id, $type, $startTime, $endTime)" }
        return this.plannedActivityRepo.findActivitiesByUserIdTypeAndDateWithoutActivity(id, type, startTime, endTime)
    }


    @Transactional
    override fun createInterval(interval: Interval): Interval {
        log.trace { "S | createInterval($interval)" }
        if (interval.intervals?.isNotEmpty() == true) {
            interval.intervals!!.forEach { createInterval(it) }
        }
        if (interval.step != null) {
            interval.step = createStep(interval.step!!)
        }
        return this.intervalRepo.save(interval)
    }

    @Transactional
    override fun createStep(step: Step): Step {
        log.trace { "S | createStep($step)" }
        return this.stepRepo.save(step)
    }


    override fun getSingleActivityForUser(userId: Long, activityId: Long): Activity {
        log.trace { "S | getSingleActivityForUser($userId, $activityId)" }

        val user = this.userRepository.findById(userId)
        if (!user.isPresent) {
            throw NotFoundException("User not found")
        }


        val activity = this.activityRepo.findById(activityId)
        if (!activity.isPresent) {
            throw NotFoundException("No activity with this id found for user")
        }

        //check if user has access to this activity

        val userObj = user.get()
        val activityObj = activity.get()

        // Athletes can only see their own activities
        if (userObj is Athlete) {
            val activitiesForUser = activityRepo.findActivitiesByUserId(userObj.id!!)
            if (activitiesForUser.none { it.id == activityObj.id!! }) {
                log.debug { "Tried to fetch activity for user other than self!" }
                throw NotFoundException("No activity with this id found for user")
            }
        } else if (userObj is Trainer) {
            // Trainers see activities of their Athletes
            var isForAthleteOfTrainer = false
            for (athlete in userObj.athletes) {
                val activitiesForAthlete = activityRepo.findActivitiesByUserId(athlete.id!!)
                if (activitiesForAthlete.any { it.id == activityObj.id!! }) {
                    isForAthleteOfTrainer = true
                }
            }
            if (!isForAthleteOfTrainer) {
                log.debug { "Tried to fetch activity for user other than self!" }
                throw NotFoundException("No activity with this id found for user")
            }
        }
        return activityObj
    }

    override fun commentActivityWithUser(userId: Long, activityId: Long, comment: CommentDTO): Comment {
        log.trace { "S | commentActivityWithUser($userId, $activityId, $comment)" }

        // check if user exists
        val userObj = userExists(userId)

        // check if activity exists
        val activityObj = activityExists(activityId)

        // check if user can access activity
        canUserAccessActivity(userId, activityId)

        val commentObj = comment.toEntity()

        // author is the currently logged-in user
        commentObj.author = userObj
        commentObj.date = LocalDateTime.now()
        commentObj.text = sanitizer.sanitizeText(commentObj.text)

        validator.validateComment(commentObj)

        activityObj.comments.add(commentObj)
        activityRepo.saveAndFlush(activityObj)

        // send notification to the other involved party (trainer if athlete comments, athlete if trainer comments)
        val notificationHeader = "new comment"
        val commentText = if (comment.text.length > 100) comment.text.substring(0, 100) + "..." else comment.text
        val notificationBody = userObj.name + " commented on your activity: " + commentText
        val notificationLink = "activity/finished/" + activityObj.id
        val notificationType = NotificationType.COMMENT

        sendNotificationToOtherParty(userId, activityObj, userObj, notificationHeader, notificationBody, notificationLink, notificationType)

        return commentObj
    }

    override fun rateActivityWithUser(userId: Long, activityId: Long, rating: Int) {
        log.trace { "S | rateActivityWithUser($userId, $activityId, $rating)" }

        // check if user exists
        val userObj = userExists(userId)

        // check if activity exists
        val activityObj = activityExists(activityId)

        // check if user can access activity
        canUserAccessActivity(userId, activityId)

        validator.validateRating(rating)

        if (userObj is Athlete) {
            activityObj.ratingAthlete = rating.toInt()
        } else if (userObj is Trainer) {
            activityObj.ratingTrainer = rating.toInt()
        }

        activityRepo.saveAndFlush(activityObj)

        // send notification to the other involved party (trainer if athlete comments, athlete if trainer comments)
        val notificationHeader = "new rating"
        val notificationBody = userObj.name + " rated your activity"
        val notificationLink = "activity/finished/" + activityObj.id
        val notificationType = NotificationType.RATING

        sendNotificationToOtherParty(userId, activityObj, userObj, notificationHeader, notificationBody, notificationLink, notificationType)
    }

    // send notification to other user when commenting/rating an activity
    // notify trainer when user comments and vice versa
    private fun sendNotificationToOtherParty(
        userId: Long,
        activityObj: Activity,
        userObj: User,
        notificationHeader: String,
        notificationBody: String,
        notificationLink: String,
        notificationType: NotificationType
    ) {
        log.trace { "S | sendNotificationToOtherParty($userId, $activityObj, $userObj, $notificationHeader, $notificationBody, $notificationLink, $notificationBody)" }

        if (userId == activityObj.user?.id && userObj is Athlete) {
            // get the trainer of the user
            val trainerId = userObj.trainer?.id
            if (trainerId != null) {
                notificationService.sendNotification(
                    trainerId,
                    notificationHeader,
                    notificationBody,
                    notificationLink,
                    notificationType
                )
            }
        } else if (userObj is Trainer) {
            // if the commenting/rating user is a trainer, send the notification to the athlete who owns the activity
            val activityUser = activityObj.user
            if (activityUser != null && activityUser is Athlete) {
                val activityUserId = activityUser.id
                if (activityUser.trainer?.id == userId && activityUserId != null) {
                    notificationService.sendNotification(
                        activityUserId,
                        notificationHeader,
                        notificationBody,
                        notificationLink,
                        notificationType
                    )
                }
            }
        }
    }

    fun mapFitActivityTypeToActivityType(sport: Sport): MyActivityType {
        return when (sport) {
            Sport.SWIMMING -> MyActivityType.SWIM
            Sport.RUNNING -> MyActivityType.RUN
            Sport.CYCLING -> MyActivityType.BIKE
            Sport.CROSS_COUNTRY_SKIING -> MyActivityType.CROSSCOUNTRYSKIING
            Sport.ROWING -> MyActivityType.ROW
            else -> MyActivityType.OTHER
        }
    }

    fun mapFitIntensityToStepType(fitIntensity: Intensity?): StepType {
        return when (fitIntensity) {
            Intensity.RECOVERY -> StepType.RECOVERY
            Intensity.ACTIVE -> StepType.ACTIVE
            Intensity.WARMUP -> StepType.WARMUP
            Intensity.COOLDOWN -> StepType.COOLDOWN
            Intensity.INTERVAL -> StepType.ACTIVE
            else -> StepType.RECOVERY
        }
    }


    fun convertMetersPerSecondToSecondsPerKilometer(speedInMetersPerSecond: Float): Int {
        log.trace { "S | convertMetersPerSecondToSecondsPerKilometer($speedInMetersPerSecond)" }
        return (1000 / speedInMetersPerSecond).toInt()
    }


    private fun convertMetersPerSecondToKilometerPerHour(enhancedSpeed: Float?): Int {
        log.trace { "S | convertMetersPerSecondToKilometerPerHour($enhancedSpeed)" }
        return (enhancedSpeed!! * 3.6).toInt()
    }

    fun isBetween(value: Int, from: Int, to: Int): Int {
        log.trace { "S | isBetween($value, $from, $to)" }
        return if (value in from..to) 1 else 0
    }

    private fun userExists(userId: Long): User {
        log.trace { "S | userExists($userId)" }
        val user = this.userRepository.findById(userId)
        if (!user.isPresent) {
            throw NotFoundException("User not found")
        }
        return user.get()
    }

    private fun activityExists(activityId: Long): Activity {
        log.trace { "S | activityExists($activityId)" }
        val activity = this.activityRepo.findById(activityId)
        if (!activity.isPresent) {
            log.debug { "Tried to fetch nonexistent activity" }
            throw NotFoundException("No such activity")
        }
        return activity.get()
    }

    private fun canUserAccessActivity(userId: Long, activityId: Long) {
        log.trace { "S | canUserAccessActivity($userId, $activityId)" }
        val activities = getAllActivities(userId, null, null)
        if (!activities.map { it.id }.contains(activityId)) {
            log.debug { "Tried to fetch activity for user who has no access to it" }
            throw NotFoundException("No activity with this id found for user")
        }
    }

    private fun calculateTimeAndLoad(plannedActivity: PlannedActivity): PlannedActivity {
        val result = calculateTimeAndLoad(plannedActivity.interval, plannedActivity.type, 0, Load.LOW)

        plannedActivity.estimatedDuration = timeDateUtil.roundTime(result.first)
        plannedActivity.load = result.second


        return plannedActivity
    }

    private fun calculateTimeAndLoad(interval: Interval, activityType: ActivityType, totalTime: Int, totalLoad: Load): Pair<Int, Load> {
        var time = totalTime
        var stepLoad = totalLoad
        if (interval.intervals?.isNotEmpty() == true) {
            for (subInterval in interval.intervals!!) {
                val result = calculateTimeAndLoad(subInterval, activityType, totalTime, totalLoad)
                time += result.first * subInterval.repeat
                if (result.second > stepLoad) {
                    stepLoad = result.second
                }
            }
        } else if (interval.step != null) {
            val step = interval.step!!
            val baseSpeed = timeDateUtil.getBaseSpeed(activityType)
            val stepTime = calculateTime(step, baseSpeed)
            val tmpLoad = calculateLoad(totalLoad, step, baseSpeed)
            if (tmpLoad > stepLoad) {
                stepLoad = tmpLoad
            }
            time = totalTime + stepTime * interval.repeat
        }
        return Pair(time, stepLoad)
    }

    private fun calculateLoad(totalLoad: Load, step: Step, baseSpeed: Double): Load {
        var currentLoad = Load.LOW
        when (step.targetType) {
            StepTargetType.CADENCE -> {
                if (step.targetFrom!! > 90) {
                    currentLoad = Load.HIGH
                } else if (step.targetFrom!! > 70) {
                    currentLoad = Load.MEDIUM
                }
            }

            StepTargetType.HEARTRATE -> {
                if (step.targetFrom!! > 170) {
                    currentLoad = Load.HIGH
                } else if (step.targetFrom!! > 150) {
                    currentLoad = Load.MEDIUM
                }
            }

            StepTargetType.PACE -> {
                if (step.targetFrom!! > baseSpeed * 1.2) {
                    currentLoad = Load.LOW
                } else if (step.targetFrom!! < baseSpeed * 0.8) {
                    currentLoad = Load.HIGH
                } else {
                    currentLoad = Load.MEDIUM
                }
            }

            StepTargetType.SPEED -> {
                if (step.targetFrom!! > baseSpeed * 1.2) {
                    currentLoad = Load.HIGH
                } else if (step.targetFrom!! < baseSpeed * 0.8) {
                    currentLoad = Load.LOW
                } else {
                    currentLoad = Load.MEDIUM
                }
            }

            null -> {}
        }
        if (totalLoad > currentLoad) { // we only want the highest load
            currentLoad = totalLoad
        }
        return currentLoad
    }

    private fun calculateTime(step: Step, baseSpeed: Double): Int {

        when (step.durationType) {
            StepDurationType.DISTANCE -> {
                if (step.durationDistanceUnit == StepDurationUnit.KM) {
                    return (step.durationDistance!! * baseSpeed / 60).toInt()
                } else {
                    return (step.durationDistance!! / 1000 * baseSpeed / 60).toInt()
                }
            }

            StepDurationType.TIME -> {
                return if (step.durationDistanceUnit == StepDurationUnit.MIN) {
                    step.durationDistance!!
                } else {
                    step.durationDistance!! / 60
                }
            }

            StepDurationType.LAPBUTTON -> {
                return 10
            }

            null -> return 0
        }
    }


}
