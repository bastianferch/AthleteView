package ase.athlete_view.domain.activity.service.impl

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.activity.persistence.*
import ase.athlete_view.domain.activity.pojo.entity.*
import ase.athlete_view.domain.activity.pojo.util.StepDurationType
import ase.athlete_view.domain.activity.pojo.util.StepTargetType
import ase.athlete_view.domain.activity.pojo.util.StepType
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.activity.service.validator.ActivityValidator
import ase.athlete_view.domain.activity.util.FitParser
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import com.garmin.fit.Intensity
import com.garmin.fit.LapMesg
import com.garmin.fit.LapTrigger
import ase.athlete_view.domain.activity.pojo.util.ActivityType as MyActivityType
import com.garmin.fit.ActivityType as FitActivityType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.ZoneId

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
    private val log = KotlinLogging.logger {}

    @Transactional
    override fun createPlannedActivity(plannedActivity: PlannedActivity, userId: Long): PlannedActivity {
        log.trace { "S | createPlannedActivity \n $plannedActivity" }

        // get the logged-in user
        val user = userRepository.findById(userId)
        if (!user.isPresent) {
            throw BadCredentialsException("User not found")
        }

        // activity is always created by the logged-in user
        plannedActivity.createdBy = user.get()

        validator.validateNewPlannedActivity(plannedActivity, user.get())
        createInterval(plannedActivity.interval)
        return this.plannedActivityRepo.save(plannedActivity)
    }

    override fun getPlannedActivity(id: Long, userId: Long): PlannedActivity {
        log.trace { "S | getPlannedActivity $id" }

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
            if (userObject.activities.none { it.id == id }) {
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

    override fun getAllPlannedActivities(userId: Long): List<PlannedActivity> {
        log.trace { "S | getAllPlannedActivities" }

        // get the logged-in user
        val user = userRepository.findById(userId)
        if (!user.isPresent) {
            throw BadCredentialsException("User not found!")
        }

        val userObject = user.get()

        // Athletes can only see their own activities
        if (userObject is Athlete) {
            return userObject.activities
        } else if (userObject is Trainer) {
            // Trainers see activities of their Athletes and their own templates
            var result: List<PlannedActivity> = userObject.activities
            for (athlete in userObject.athletes) {
                result = result + athlete.activities
            }
            return result
        }

        return listOf()
    }

    override fun updatePlannedActivity(id: Long, plannedActivity: PlannedActivity, userId: Long): PlannedActivity {
        log.trace { "S | updatePlannedActivity $id $plannedActivity" }

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
    override fun importActivity(files: List<MultipartFile>, userId: Long): Unit {
        log.debug { "Ready to parse ${files.size} (${files[0].name}) files for user w/ ID $userId" }

        val user = userRepository.findById(userId)

        if (!user.isPresent) {
            throw BadCredentialsException("User not found")
        }

        val ids = arrayOf<String>().toMutableList()
        for (item in files) {
            val data = fitParser.decode(item.inputStream)

            if (data.recordMesgs.size == 0) {
                continue // skip empty file
            }

            var powerSum = 0
            var hrSum = 0
            var calSum = 0
            var totalDistance = 0.0
            var cadenceSum = 0
            var hrMax: Short = 0
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
            var plannedActivity : PlannedActivity? = null

            if (compare) {
                val fitActivityType = data.recordMesgs[0].activityType
                val date = data.recordMesgs[0].timestamp.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                val activityType = mapFitActivityTypeToActivityType(fitActivityType)
                val plannedActivityList = getPlannedActivityByTypeUserIdAndDate(userId, activityType, date)
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



            for (d in data.recordMesgs) {
                // add every lap to the list for later saving in the repo
                if (lap.timestamp < d.timestamp) {
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
                            log.debug { "$curLapIntensity  changed to  ${lap.intensity}" }
                            curLapIntensity = lap.intensity
                            j++
                        }
                    } else if (sameDurations) {
                        if (lastLap.lapTrigger == stepList!![j].durationType) {
                            j++
                        }
                    }
                    log.debug { "${stepList!![j].targetType} ${stepList[j].targetFrom} ${stepList[j].targetTo} ${convertMetersPerSecondToSecondsPerKilometer(d.enhancedSpeed)}  " }
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
                val dist = d.distance ?: 0.0f
                val power = d.power ?: 0
                val cal = d.calories ?: 0
                val cadence = d.cadence ?: 0

                if (hr > hrMax) {
                    hrMax = hr.toShort()
                }

                if (power > powerMax) {
                    powerMax = power
                }

                calSum += cal
                powerSum += power
                hrSum += hr
                totalDistance += dist
                cadenceSum += cadence
            }


            val totalElems = data.recordMesgs.size
            val avgBpm = hrSum / totalElems
            val avgPower = powerSum / totalElems
            val avgCadence = cadenceSum / totalElems
            val accuracy = ((accuracySum.toFloat() / (totalElems - intensityValueMissing)) * 100).toInt()

            // if accuracy too low do not count as planned activity

            plannedActivity = if (compare && accuracy < 25) null else plannedActivity

            val fitId: String = fitFileRepo.saveFitData(item)
            log.debug { "accuracy: $accuracy $accuracySum $totalElems $intensityValueMissing" }

            val activity = Activity(
                null,
                user.get(),
                accuracy,
                avgBpm,
                hrMax.toInt(),
                totalDistance,
                calSum,
                avgCadence,
                avgPower,
                powerMax,
                0,
                1, // TODO implement if time left
                fitId,
                plannedActivity,
                laps
            )
            plannedActivity?.activity = activity
            ids.add(fitId)
            laps.map { lapRepo.save(it) }
            val respData = activityRepo.save(activity)
            log.debug { respData.toString() }
        }
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

    private fun compareLapDurations(stepList: List<Step>, lapList: List<LapMesg>): Boolean {
        var i = 0
        for (step in stepList) { // go through all steps
            when (step.durationType) {
                StepDurationType.LAPBUTTON -> {
                    while (lapList[i].lapTrigger != LapTrigger.MANUAL) {
                        if (i == lapList.size) { // all laps are done and more steps
                            return false
                        }
                        i++
                    }
                }

                StepDurationType.TIME -> {
                    while (lapList[i].lapTrigger != LapTrigger.TIME) {
                        if (i == lapList.size) { // all laps are done and more steps
                            return false
                        }
                        i++
                    }
                }

                StepDurationType.DISTANCE -> {
                    while (lapList[i].lapTrigger != LapTrigger.DISTANCE) {
                        if (i == lapList.size) { // all laps are done and more steps
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

    private fun getPlannedActivityByTypeUserIdAndDate(id: Long, type: MyActivityType, date: LocalDate): List<PlannedActivity> {
        return this.plannedActivityRepo.findActivitiesByUserIdTypeAndDateWithoutActivity(id, type, date)
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
