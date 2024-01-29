package ase.athlete_view.domain.csp.service.impl

import ase.athlete_view.common.exception.entity.AlreadyExistsException
import ase.athlete_view.common.exception.entity.InternalException
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.util.Load
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.csp.persistence.CspRepository
import ase.athlete_view.domain.csp.pojo.dto.CspDto
import ase.athlete_view.domain.csp.pojo.entity.CspJob
import ase.athlete_view.domain.csp.service.CspService
import ase.athlete_view.domain.csp.util.QueueRequestSender
import ase.athlete_view.domain.notification.service.NotificationService
import ase.athlete_view.domain.time_constraint.pojo.dto.WeeklyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.service.TimeConstraintService
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.*
import java.time.format.DateTimeFormatter

@Service
class CspServiceImpl(private val cspRepository: CspRepository, private val mapper: ObjectMapper, private val queueSender: QueueRequestSender, private val constraintService: TimeConstraintService, private val userService: UserService, private val activityService: ActivityService, private val notificationService: NotificationService) : CspService {
    private val log = KotlinLogging.logger {}

    companion object {
        const val SLOT_DURATION = 15 // Duration of each time slot in minutes
        const val SLOTS_PER_HOUR = 60 / SLOT_DURATION // Number of slots per hour
        const val TOTAL_SLOTS = 24 * 60 / SLOT_DURATION // Total slots in a day
    }

    @Transactional
    override fun accept(cspDto: CspDto, userId: Long) {
        log.trace { "S | accept($cspDto, $userId)" }
        val validation = validate(cspDto, userId)

        if (getJob(userId) != null) {
            throw AlreadyExistsException("Job already exists for next week")
        }


        if (validation.size > 0) {
            throw ValidationException(validation.joinToString(" "))
        }


        val jsonObject = createJsonAndPersist(cspDto, userId)

        queueSender.sendMessage(mapper.writeValueAsString(jsonObject))
    }

    @Transactional
    override fun revertJob(userId: Long) {
        log.trace { "S | revertJob($userId)" }
        val temp = getJob(userId)
        if (temp == null) {
            throw NotFoundException("There is no Job for next week.")
        }
        notifyAthletesOfRevert(temp.activities)
        activityService.deletePlannedActivities(temp.activities)
        cspRepository.delete(temp)
    }

    fun notifyAthletesOfRevert(activities: MutableList<PlannedActivity>) {
        val athletes: MutableSet<Long> = mutableSetOf()
        for (activity in activities) {
            athletes.add(activity.createdFor!!.id!!)
        }
        for(id in athletes){
            notificationService.sendNotification(id, "Current Trainingsplan reverted", "Your trainer has reverted the current trainingsplan. A new one will likely be scheduled soon.")
        }
    }

    override fun getJob(userId: Long): CspJob? {
        log.trace { "S | getJob($userId)" }
        try {
            val trainer = userService.getById(userId)
            if (trainer.getUserType() != "trainer") {
                throw ValidationException("User $userId is not a trainer.")
            }

            val currentTimestamp = Instant.now().toEpochMilli()
            val today = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTimestamp), ZoneId.systemDefault())
            val nextMonday = today.with(DayOfWeek.MONDAY).plusWeeks(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            val formattedNextMonday = nextMonday.format(formatter)

            val temp = cspRepository.findJobByTrainerAndDate(trainer as Trainer, formattedNextMonday)

            return if (temp != null && temp.isNotEmpty()) {
                if (temp.size > 1) {
                    throw InternalException("Multiple Jobs exist for next week.")
                }
                temp[0]
            } else {
                null
            }

        } catch (e: NotFoundException) {
            throw ValidationException("User $userId could not be found.")
        }
    }

    fun validate(cspDto: CspDto, userId: Long): MutableList<String> {
        log.trace { "S | validate($cspDto, $userId)" }

        val errors: MutableList<String> = ArrayList()

        val currentTimestamp = Instant.now().toEpochMilli()
        val today = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTimestamp), ZoneId.systemDefault())
        val nextMonday = today.with(DayOfWeek.MONDAY).plusWeeks(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
        val followingMonday = nextMonday.plusWeeks(1)

        try {
            val trainer = userService.getById(userId)
            if (trainer.getUserType() != "trainer") {
                errors.add("Current user is not a trainer.")
            }

            val trainerTableSum = createConstraintTable(trainer, nextMonday, followingMonday, true).flatten().count { it } * SLOT_DURATION
            var trainerDurationSum = 0
            for (mapping in cspDto.mappings) {
                if (mapping.activities.size > 7) {
                    val tempAthlete = userService.getById(mapping.userId)
                    errors.add("Athlete ${tempAthlete.name} can not have more than 1 activitiy assigned per day (7 a week).")
                }

                try {
                    val athlete = userService.getById(mapping.userId)

                    val tableSum = createConstraintTable(athlete, nextMonday, followingMonday, false).flatten().count { it } * SLOT_DURATION

                    var intensitySum = 0
                    var durationSum = 0

                    for (activity in mapping.activities) {
                        try {
                            val tempActivity = activityService.getPlannedActivity(activity.id, userId)
                            if (tempActivity.template) {
                                if (tempActivity.load == Load.HIGH) {
                                    intensitySum++
                                }

                                if (tempActivity.estimatedDuration != null) {
                                    durationSum += tempActivity.estimatedDuration!!
                                }

                                if (activity.withTrainer && tempActivity.estimatedDuration != null) {
                                    trainerDurationSum += tempActivity.estimatedDuration!!
                                }

                                if (tempActivity.createdBy?.id != userId) {
                                    errors.add("Activity ${activity.id} was not created by Trainer $userId.")
                                }

                                if (!(trainer as Trainer).athletes.contains(athlete)) {
                                    val tempAthlete = userService.getById(athlete.id!!)
                                    errors.add("Athlete ${tempAthlete.name} is not assigned to Trainer $userId.")
                                }

                            }
                        } catch (e: NotFoundException) {
                            errors.add("Activity ${activity.id} could not be found.")
                        }
                    }


                    if (intensitySum > 4) {
                        val tempAthlete = userService.getById(athlete.id!!)
                        errors.add("Athlete ${tempAthlete.name} has too much intensity.")
                    }

                    if (durationSum > tableSum) {
                        val tempAthlete = userService.getById(athlete.id!!)
                        errors.add("Athlete ${tempAthlete.name} does not have enough timeslots.")
                    }

                } catch (e: NotFoundException) {
                    val tempAthlete = userService.getById(mapping.userId)
                    errors.add("Athlete ${tempAthlete.name} could not be found.")
                }
            }

            if (trainerDurationSum > trainerTableSum) {
                errors.add("Trainer does not have enough timeslots.")
            }
        } catch (e: NotFoundException) {
            errors.add("Current user could not be found.")
        }

        return errors
    }

    fun createJsonAndPersist(cspDto: CspDto, userId: Long): Map<String, Any> {
        log.trace { "S | createJsonAndPersist($cspDto, $userId)" }
        val currentTimestamp = Instant.now().toEpochMilli()
        val today = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTimestamp), ZoneId.systemDefault())
        val nextMonday = today.with(DayOfWeek.MONDAY).plusWeeks(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
        val followingMonday = nextMonday.plusWeeks(1)

        val activities = mutableListOf<Map<String, Any>>()
        val trainerTable = mutableListOf<List<Boolean>>()
        val athleteTables = mutableMapOf<String, List<List<Boolean>>>()

        val trainer = userService.getById(userId)
        trainerTable.addAll(createConstraintTable(trainer, nextMonday, followingMonday, true))

        val activityEntities: MutableList<PlannedActivity> = ArrayList()

        for (mapping in cspDto.mappings) {

            val athlete = userService.getById(mapping.userId)

            athleteTables[mapping.userId.toString()] = createConstraintTable(athlete, nextMonday, followingMonday, false)

            for (activity in mapping.activities) {
                val templateActivity = activityService.getPlannedActivity(activity.id, userId)
                val duplicatedActivity = activityService.createPlannedActivity(templateActivity.copyWithNewCreatedForAndWithTrainer(athlete as Athlete, activity.withTrainer), userId, isCsp = true)
                activityEntities.add(duplicatedActivity)
                if (!templateActivity.template) {
                    continue
                }
                val duplicatedActivityJson = mapOf(
                        "duration" to ((duplicatedActivity.estimatedDuration as Int) / SLOT_DURATION) as Any,
                        "intensity" to loadToInteger(duplicatedActivity.load!!) as Any,
                        "withTrainer" to duplicatedActivity.withTrainer as Any, //type boolean
                        "athlete" to mapping.userId as Any, // type int
                        "id" to duplicatedActivity.id as Any // type int
                )
                activities.add(duplicatedActivityJson)
            }
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val formattedNextMonday = nextMonday.format(formatter)

        val job: CspJob = CspJob(null, activityEntities, trainer as Trainer, formattedNextMonday)
        cspRepository.save(job)

        return mapOf(
                "trainerId" to userId,
                "activities" to activities,
                "requestTimestamp" to currentTimestamp,
                "schedule" to mapOf(
                        "trainerTable" to trainerTable,
                        "athleteTables" to athleteTables,
                        "trainerId" to userId,
                )
        )
    }

    fun loadToInteger(load: Load): Int {
        return when (load) {
            Load.LOW -> 0
            Load.MEDIUM -> 1
            Load.HIGH -> 2
        }
    }

    fun createConstraintTable(user: User, nextMonday: ZonedDateTime, followingMonday: ZonedDateTime, trainer: Boolean): List<List<Boolean>> {
        log.trace { "S | createConstraintTable($user, $nextMonday, $followingMonday, $trainer)" }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")

        val formattedNextMonday = nextMonday.format(formatter)
        val formattedFollowingMonday = followingMonday.format(formatter)

        val ret: MutableList<List<Boolean>> = mutableListOf()

        val constraints = (constraintService.getAll(user.toUserDTO(), "weekly", formattedNextMonday, formattedFollowingMonday)) as List<WeeklyTimeConstraintDto>
        for (dayOfWeek in DayOfWeek.values()) {

            val filteredElements = constraints.filter { it.constraint.weekday == dayOfWeek }
            val freeConstraints = filteredElements.filter { !it.isBlacklist }
            val blockedConstraints = filteredElements.filter { it.isBlacklist }

            val timeSlots = MutableList(TOTAL_SLOTS) { false }

            freeConstraints.forEach {
                val startSlot = it.constraint.startTime.hour * SLOTS_PER_HOUR + it.constraint.startTime.minute / SLOT_DURATION
                val endSlot = it.constraint.endTime.hour * SLOTS_PER_HOUR + it.constraint.endTime.minute / SLOT_DURATION
                for (i in startSlot until endSlot) {
                    timeSlots[i] = true
                }
            }

            blockedConstraints.forEach {
                val startSlot = it.constraint.startTime.hour * SLOTS_PER_HOUR + it.constraint.startTime.minute / SLOT_DURATION
                val endSlot = it.constraint.endTime.hour * SLOTS_PER_HOUR + it.constraint.endTime.minute / SLOT_DURATION
                for (i in startSlot until endSlot) {
                    timeSlots[i] = false
                }
            }

            activityService.getAllPlannedActivities(user.id!!, nextMonday.toLocalDateTime(), followingMonday.toLocalDateTime()).forEach {
                if (it.date == null) {
                    return@forEach
                }

                if (it.template) {
                    return@forEach
                }

                if (!trainer || trainer && it.withTrainer) {
                    val startSlot = (it.date!!.hour * SLOTS_PER_HOUR) + (it.date!!.minute / SLOT_DURATION)
                    val endSlot = ((it.date!!.hour + (it.estimatedDuration!! / 60)) * SLOTS_PER_HOUR) + ((it.date!!.minute + (it.estimatedDuration!! % 60)) / SLOT_DURATION)
                    for (i in startSlot until endSlot) {
                        timeSlots[i] = false
                    }
                }
            }

            ret.add(timeSlots)

        }

        return ret
    }
}
