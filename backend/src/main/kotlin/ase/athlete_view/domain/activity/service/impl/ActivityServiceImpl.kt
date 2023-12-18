package ase.athlete_view.domain.activity.service.impl

import  ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.activity.persistence.*
import ase.athlete_view.domain.activity.pojo.entity.*
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import ase.athlete_view.domain.activity.pojo.util.StepDurationType
import ase.athlete_view.domain.activity.pojo.util.StepTargetType
import ase.athlete_view.domain.activity.pojo.util.StepType
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.activity.service.validator.ActivityValidator
import ase.athlete_view.domain.activity.util.FitParser
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import com.garmin.fit.FitMessages
import com.garmin.fit.Intensity
import com.garmin.fit.LapMesg
import com.garmin.fit.LapTrigger
import com.garmin.fit.RecordMesg
import ase.athlete_view.domain.activity.pojo.util.ActivityType as MyActivityType
import com.garmin.fit.ActivityType as FitActivityType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.ZoneOffset
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
    private val fitFileRepo: FitDataRepositoryImpl
) : ActivityService {
    private val logger = KotlinLogging.logger {}

    @Transactional
    override fun createPlannedActivity(plannedActivity: PlannedActivity, userId: Long): PlannedActivity {
        logger.trace { "S | createPlannedActivity \n $plannedActivity" }

        // get the logged-in user
        val user = userRepository.findById(userId)
        if (!user.isPresent) {
            throw BadCredentialsException("User not found")
        }

        // activity is always created by the logged-in user
        val usr = user.get()
        validator.validateNewPlannedActivity(plannedActivity, usr)
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

    override fun getPlannedActivity(id: Long, userId: Long): PlannedActivity {
        logger.trace { "S | getPlannedActivity $id" }

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
            // TODO: does this also need to be adapted to use `createdFor`?
            val isOwnTemplate = userObject.activities.any { it.id == id }
            var isForAthleteOfTrainer = false
            for (athlete in userObject.athletes) {
                // TODO: same here, think this only considers those `createdBy` athlete, none that were created for them by trainer
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

    @Transactional
    override fun getAllPlannedActivities(userId: Long, startDate: LocalDateTime?, endDate: LocalDateTime?): List<PlannedActivity> {
        logger.trace { "S | getAllPlannedActivities" }

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

    override fun updatePlannedActivity(id: Long, plannedActivity: PlannedActivity, userId: Long): PlannedActivity {
        logger.trace { "S | updatePlannedActivity $id $plannedActivity" }

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
        plannedActivity.interval = createInterval(plannedActivity.interval)
        return this.plannedActivityRepo.save(plannedActivity)
    }

    @Transactional
    override fun importActivity(files: List<MultipartFile>, userId: Long): Activity {
        logger.trace { "S | Ready to parse ${files.size} (${files[0].name}) files for user w/ ID $userId" }

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

    override fun calculateStats(data: FitMessages, user: User, item: MultipartFile): Pair<Activity, String> {
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
        var compare = data.recordMesgs[0].activityType != null
        var sameStructure = false
        var sameDurations = false
        var stepList: List<Step>? = null
        var plannedActivity: PlannedActivity? = null

        if (compare) {
            val fitActivityType = data.recordMesgs[0].activityType
            val startTime = LocalDateTime.ofEpochSecond(data.recordMesgs[0].timestamp.timestamp + 631065600, 0, ZoneOffset.UTC)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
            val endTime = LocalDateTime.ofEpochSecond(data.recordMesgs[0].timestamp.timestamp + 631065600, 0, ZoneOffset.UTC)
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(0)
            val activityType = mapFitActivityTypeToActivityType(fitActivityType)

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

        for (d in data.recordMesgs) {
            if (startTime === null) {
                // https://developer.garmin.com/fit/cookbook/datetime/
                startTime = LocalDateTime.ofEpochSecond(d.timestamp!!.timestamp + 631065600, 0, ZoneOffset.UTC)
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

        endTime = LocalDateTime.ofEpochSecond(recordMsgHolder!!.timestamp.timestamp + 631065600, 0, ZoneOffset.UTC)

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
            data.recordMesgs[0].activityType?.let { mapFitActivityTypeToActivityType(it) }
        )
        plannedActivity?.activity = activity
        laps.map { lapRepo.save(it) }

        return activityRepo.save(activity) to fitId
    }

    private fun compareLapLists(stepList: List<Step>, lapList: List<LapMesg>): Boolean {
        var i = 0
        for (lap in lapList) { // go through all laps
            val stepIntensity = stepList[i].type
            val lapIntensity = mapFitIntensityToStepType(lap.intensity)
            if (lapIntensity != stepIntensity) {
                if (i == stepList.size) { // all steps are done and more laps
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
        logger.trace { "S | getAllActivities" }
        val user = userRepository.findById(uid).getOrNull()
            ?: throw NotFoundException("No such user!")

        return if (startDate != null && endDate != null) {
            activityRepo.findActivitiesByUserAndDateRange(user.id!!, startDate, endDate)
        } else {
            activityRepo.findActivitiesByUserId(uid)
        }
    }

    private fun compareLapDurations(stepList: List<Step>, lapList: List<LapMesg>): Boolean {
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
        return this.plannedActivityRepo.findActivitiesByUserIdTypeAndDateWithoutActivity(id, type, startTime, endTime)
    }

    override fun getSingleActivityForUser(userId: Long, activityId: Long): Activity {
        logger.trace { "S | getSingleActivityForUser($userId, $activityId)" }

        val user = this.userRepository.findById(userId)
        if (!user.isPresent) {
            throw NotFoundException("User not found")
        }


        val activity = this.activityRepo.findById(activityId)
        if (!activity.isPresent) {
            throw NotFoundException("No such activity")
        }

        val userObj = user.get()
        val activityObj = activity.get()

        if (userObj != activityObj.user) {
            logger.debug { "Tried to fetch activity for user other than self!" }
            throw NotFoundException("No activity with this id found for user")
        }

        return activityObj
    }


    override fun createInterval(interval: Interval): Interval {
        if (interval.intervals?.isNotEmpty() == true) {
            interval.intervals!!.forEach { createInterval(it) }
        }
        if (interval.step != null) {
            interval.step = createStep(interval.step!!)
        }
        return this.intervalRepo.save(interval)
    }

    private fun createStep(step: Step): Step {
        return this.stepRepo.save(step)
    }


    // TODO find the different kind of sports and why rowing and crosscountryskiing are not existing in FitActivityType
    fun mapFitActivityTypeToActivityType(fitActivityType: FitActivityType): MyActivityType {
        return when (fitActivityType) {
            FitActivityType.SWIMMING -> MyActivityType.SWIM
            FitActivityType.RUNNING -> MyActivityType.RUN
            FitActivityType.CYCLING -> MyActivityType.BIKE
            FitActivityType.FITNESS_EQUIPMENT -> MyActivityType.ROW
            FitActivityType.GENERIC -> MyActivityType.CROSSCOUNTRYSKIING
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
        return (1000 / speedInMetersPerSecond).toInt()
    }


    private fun convertMetersPerSecondToKilometerPerHour(enhancedSpeed: Float?): Int {
        return (enhancedSpeed!! * 3.6).toInt()
    }

    fun isBetween(value: Int, from: Int, to: Int): Int {
        return if (value in from..to) 1 else 0
    }
}
