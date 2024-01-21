package ase.athlete_view.domain.csp.util

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.csp.service.CspService
import ase.athlete_view.domain.csp.service.impl.CspServiceImpl.Companion.SLOTS_PER_HOUR
import ase.athlete_view.domain.csp.service.impl.CspServiceImpl.Companion.SLOT_DURATION
import ase.athlete_view.domain.notification.service.NotificationService
import ase.athlete_view.domain.time_constraint.pojo.dto.WeeklyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.service.TimeConstraintService
import ase.athlete_view.domain.user.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.core.Message
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters


@Component
class QueueResponseListener(private val objectMapper: ObjectMapper,
                            private val cspService: CspService,
                            private val activityService: ActivityService,
                            private val notificationService: NotificationService,
                            private val userService: UserService,
                            private val constraintService: TimeConstraintService) {

    private val logger = KotlinLogging.logger {}

    fun receiveMessage(message: String) {
        logger.debug { "Received response through queue in wrong format: $message" }
    }

    fun receiveMessage(message: ByteArray) {
        var trainerId: Long? = null
        try {
            var messageBody = String(message, StandardCharsets.UTF_8)
            logger.debug { "Received response through queue: $messageBody" }

            val resultMap: Map<String, Any> = objectMapper.readValue(messageBody)

            trainerId = (resultMap["trainerId"] as Int).toLong()

            val activities: List<Map<String, Any>> = resultMap["activities"] as List<Map<String, Any>>

            if (!(resultMap["success"] as Boolean)) {
                logger.error { "Received error response from queue: ${resultMap["error"]}" }
                notificationService.sendNotification(trainerId, "Scheduling Job failed", resultMap["error"] as String, "/trainingsplan")
                cspService.revertJob(trainerId)
                return
            }

            val changed = (resultMap["threshold"] as Int) != 0

            val requestTimestamp = resultMap["requestTimestamp"] as Long
            val today = ZonedDateTime.ofInstant(Instant.ofEpochMilli(requestTimestamp), ZoneId.systemDefault())
            val nextMonday = today.with(DayOfWeek.MONDAY).plusWeeks(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
            val followingMonday = nextMonday.plusWeeks(1)

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            val formattedLinkDate = nextMonday.format(formatter)

            val days = getDatesForFollowingWeek(requestTimestamp)

            val athletes: MutableMap<Long, MutableList<PlannedActivity>> = mutableMapOf()

            for (elem in activities) {
                var scheduledStartTime = elem["scheduledStartTime"] as Int
                var scheduledDay = elem["scheduledDay"] as Int
                var elemId = (elem["id"] as Int).toLong()

                val time = LocalTime.of((scheduledStartTime / SLOTS_PER_HOUR), (scheduledStartTime % SLOTS_PER_HOUR) * SLOT_DURATION)
                val date = days[scheduledDay]
                val dateTime = LocalDateTime.of(date, time)
                var temp = activityService.getPlannedActivity(elemId, trainerId)
                logger.info { "Updating activity with id $elemId with scheduled time." }
                temp = activityService.updatePlannedActivity(elemId, PlannedActivity(
                        temp.id,
                        temp.name,
                        temp.type,
                        temp.interval,
                        temp.withTrainer,
                        temp.template,
                        temp.note,
                        dateTime,
                        temp.estimatedDuration,
                        temp.load,
                        temp.createdBy,
                        temp.createdFor,
                        temp.activity
                ), trainerId)

                if (athletes.containsKey(temp.createdFor?.id!!)) {
                    athletes[temp.createdFor?.id!!]!!.add(temp)
                } else {
                    athletes[temp.createdFor?.id!!] = mutableListOf(temp)
                }

            }
            if (!changed) {
                notificationService.sendNotification(trainerId, "Scheduling Job completed", "All planned activity timestamps were updated. You can check them on your calendar/homepage.", "?date=${formattedLinkDate}")
                for (athlete in athletes) {
                    notificationService.sendNotification(athlete.key, "Trainings for next week scheduled", "Your trainer has scheduled your trainings for next week. You can check them on your calendar/homepage.", "?date=${formattedLinkDate}")
                }
            } else {
                notificationService.sendNotification(trainerId, "Scheduling Job completed", "All planned activity timestamps were updated. You can check them on your calendar/homepage. Please note that some athletes were assigned time slots outside of their available times. They may contact you.", "?date=${formattedLinkDate}")
                for (athlete in athletes) {
                    if (checkScheduledOutside(athlete.key, athlete.value, nextMonday, followingMonday)) {
                        notificationService.sendNotification(athlete.key, "Trainings for next week scheduled", "Your trainings for next week have been assigned. You can check them on your calendar/homepage. Please note that some are outside of your available times. Please try to make time for them, alternatively you can choose to do them on your own without the trainer, whenever you are free.", "?date=${formattedLinkDate}")
                    } else {
                        notificationService.sendNotification(athlete.key, "Trainings for next week scheduled", "Your trainer has scheduled your trainings for next week. You can check them on your calendar/homepage.", "?date=${formattedLinkDate}")

                    }
                }
            }

        } catch (e: Exception) {
            logger.error { "Error reading incoming response from queue: ${e.message}" }
            if (trainerId != null) {
                notificationService.sendNotification(trainerId, "Error during scheduling", "There was an error during scheduling. Please try again", "/trainingsplan")
                cspService.revertJob(trainerId)
            }
        }
    }

    fun checkScheduledOutside(id: Long, activities: MutableList<PlannedActivity>, nextMonday: ZonedDateTime, followingMonday: ZonedDateTime): Boolean {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")

        val formattedNextMonday = nextMonday.format(formatter)
        val formattedFollowingMonday = followingMonday.format(formatter)

        val user = userService.getById(id)
        var ret = true
        val constraints = (constraintService.getAll(user.toUserDTO(), "weekly", formattedNextMonday, formattedFollowingMonday)) as List<WeeklyTimeConstraintDto>
        for (activity in activities) {
            val filteredElements = constraints.filter { it.constraint.weekday == activity.date!!.dayOfWeek }
            val freeConstraints = filteredElements.filter { !it.isBlacklist }
            val blockedConstraints = filteredElements.filter { it.isBlacklist }

            for (constraint in freeConstraints) {
                val activityStart = activity.date!!.toLocalTime()
                val activityEnd = activity.date!!.plusMinutes(activity.estimatedDuration!!.toLong()).toLocalTime()
                val constraintStart = constraint.constraint.startTime
                val constraintEnd = constraint.constraint.endTime

                //(StartDate1 <= EndDate2) and (StartDate2 <= EndDate1)
                if ((constraintStart.isBefore(activityEnd) || constraintStart == activityEnd) && (activityStart.isBefore(constraintEnd) || activityStart == constraintEnd)) {
                    ret = false
                }
            }

            for (constraint in blockedConstraints) {
                val activityStart = activity.date!!.toLocalTime()
                val activityEnd = activity.date!!.plusMinutes(activity.estimatedDuration!!.toLong()).toLocalTime()
                val constraintStart = constraint.constraint.startTime
                val constraintEnd = constraint.constraint.endTime

                //(StartDate1 <= EndDate2) and (StartDate2 <= EndDate1)
                if ((constraintStart.isBefore(activityEnd) || constraintStart == activityEnd) && (activityStart.isBefore(constraintEnd) || activityStart == constraintEnd)) {
                    ret = true
                }
            }

            activityService.getAllPlannedActivities(user.id!!, nextMonday.toLocalDateTime(), followingMonday.toLocalDateTime()).forEach {
                if (it.id == activity.id) {
                    return@forEach
                }

                if (it.date == null) {
                    return@forEach
                }

                if (it.template) {
                    return@forEach
                }

                val activityStart1 = activity.date!!.toLocalTime()
                val activityEnd1 = activity.date!!.plusMinutes(activity.estimatedDuration!!.toLong()).toLocalTime()
                val activityStart2 = it.date!!.toLocalTime()
                val activityEnd2 = it.date!!.plusMinutes(activity.estimatedDuration!!.toLong()).toLocalTime()

                //(StartDate1 <= EndDate2) and (StartDate2 <= EndDate1)
                if ((activityStart1.isBefore(activityEnd2) || activityStart1 == activityEnd2) && (activityStart2.isBefore(activityEnd1) || activityStart2 == activityEnd1)) {
                    ret = true
                }
            }
        }
        return ret
    }

    fun receiveMessage(message: Message) {
        logger.debug { "Received response through queue in wrong format: ${message.body}" }
    }

    fun getDatesForFollowingWeek(timestamp: Long): List<LocalDate> {
        val today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)
        val nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0).withNano(0)

        val datesOfFollowingWeek = mutableListOf<LocalDate>()

        // Populate the list with dates from Monday to Sunday of the following week
        for (i in 0 until 7) {
            val currentDate = nextMonday.plusDays(i.toLong())
            datesOfFollowingWeek.add(currentDate.toLocalDate())
        }

        return datesOfFollowingWeek
    }
}