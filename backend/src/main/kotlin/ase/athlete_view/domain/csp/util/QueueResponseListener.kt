package ase.athlete_view.domain.csp.util

import ase.athlete_view.domain.activity.persistence.ActivityRepository
import ase.athlete_view.domain.activity.persistence.PlannedActivityRepository
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.csp.service.CspService
import ase.athlete_view.domain.csp.service.impl.CspServiceImpl.Companion.SLOTS_PER_HOUR
import ase.athlete_view.domain.csp.service.impl.CspServiceImpl.Companion.SLOT_DURATION
import ase.athlete_view.domain.notification.service.NotificationService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.core.Message
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.*
import java.time.temporal.TemporalAdjusters


@Component
class QueueResponseListener(private val objectMapper: ObjectMapper, private val cspService:CspService, private val activityService: ActivityService, private val notificationService: NotificationService) {

    private val logger = KotlinLogging.logger {}

    fun receiveMessage(message: String){
        logger.debug{"Received response through queue in wrong format: $message"}
    }

    fun receiveMessage(message:  ByteArray){
        try {
            val messageBody = String(message, StandardCharsets.UTF_8)
            logger.debug{"Received response through queue: $messageBody"}
            val resultMap:Map<String, Any> = objectMapper.readValue(messageBody)

            val trainerId = (resultMap["trainerId"] as Int).toLong()

            val activities: List<Map<String, Any>> = resultMap["activities"] as List<Map<String, Any>>

            if(!(resultMap["success"] as Boolean)) {
                logger.error{"Received error response from queue: ${resultMap["error"]}"}
                notificationService.sendNotification(trainerId,"Scheduling Job failed", resultMap["error"] as String,"/trainingsplan")
                cspService.revertJob(trainerId)
                return
            }

            val requestTimestamp = resultMap["requestTimestamp"] as Long
            val days = getDatesForFollowingWeek(requestTimestamp)

            val ids = mutableSetOf(trainerId)


            for (elem in activities){
                val time = LocalTime.of(((elem["scheduledStartTime"]as Int) / SLOTS_PER_HOUR), ((elem["scheduledStartTime"]as Int) % SLOTS_PER_HOUR) * SLOT_DURATION)
                val date = days[(elem["scheduledDay"] as Int)]
                val dateTime = LocalDateTime.of(date,time)
                val temp = activityService.getPlannedActivity((elem["id"] as Int).toLong(),trainerId)
                logger.info{"Updating activity with id ${elem["id"]} with schedule time."}
                activityService.updatePlannedActivity((elem["id"]as Int).toLong(), PlannedActivity(
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
                ),trainerId)

                ids.add(temp.createdBy?.id!!)
                ids.add(temp.createdFor?.id!!)
            }

            for(id in ids){
                notificationService.sendNotification(id,"Scheduling Job completed", "All planned activity timestamps were updated. You can check them on your calendar.","/calendar")
            }

        } catch (e: Exception) {
            logger.error{"Error reading incoming response from queue: ${e.message}"}
        }
    }

    fun receiveMessage(message: Message){
        logger.debug{"Received response through queue in wrong format: ${message.body}"}
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