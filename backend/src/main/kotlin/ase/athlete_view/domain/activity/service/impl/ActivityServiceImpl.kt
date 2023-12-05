package ase.athlete_view.domain.activity.service.impl

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.activity.persistence.*
import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.activity.service.validator.ActivityValidator
import ase.athlete_view.domain.activity.util.FitParser
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
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
        plannedActivity.createdBy = user.get()

        validator.validateNewPlannedActivity(plannedActivity, user.get())
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
        logger.trace { "S | getAllPlannedActivities" }

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
    override fun importActivity(files: List<MultipartFile>, userId: Long): Unit {
        logger.debug { "Ready to parse ${files.size} (${files[0].name}) files for user w/ ID $userId" }

        val user = userRepository.findById(userId)

        if (!user.isPresent) {
            throw BadCredentialsException("User not found")
        }

        var ids = arrayOf<String>().toMutableList()
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

            var fitActivityType = data.recordMesgs[0].activityType
            var date = LocalDate.of(1970, 1, 1)


            for (d in data.recordMesgs) {
                logger.debug { d.toString() }

                if (date == LocalDate.of(1970, 1, 1)) {
                    date = d.timestamp.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                }
                if (fitActivityType == null) {
                    fitActivityType = FitActivityType.INVALID
                } else if (fitActivityType != d.activityType && fitActivityType != FitActivityType.GENERIC && fitActivityType != FitActivityType.INVALID) {
                    fitActivityType = FitActivityType.GENERIC
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
            logger.debug { "date: $date" }
            logger.debug { "activityType: $fitActivityType" }
            val activityType = mapFitActivityTypeToActivityType(fitActivityType)
            val plannedActivityList = getPlannedActivityByTypeUserIdAndDate(userId, activityType, date)


            val totalElems = data.recordMesgs.size
            val avgBpm = hrSum / totalElems
            val avgPower = powerSum / totalElems
            val avgCadence = cadenceSum / totalElems

            val fitId: String = fitFileRepo.saveFitData(item)

            val activity = Activity(
                null,
                user.get(),
                0, // TODO: implement
                avgBpm,
                hrMax.toInt(),
                totalDistance,
                calSum,
                avgCadence,
                avgPower,
                powerMax,
                0,
                1, // TODO implement
                fitId,
                if (plannedActivityList.isNotEmpty()) plannedActivityList[0] else null
            )

            ids.add(fitId)
            val respData = activityRepo.save(activity)
            logger.debug { respData.toString() }
        }
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
}
