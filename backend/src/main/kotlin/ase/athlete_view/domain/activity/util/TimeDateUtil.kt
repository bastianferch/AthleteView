package ase.athlete_view.domain.activity.util

import ase.athlete_view.domain.activity.pojo.util.ActivityType
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset

const val GARMIN_OFFSET_S = 631065600
const val timeSteps = 15

@Component
class TimeDateUtil {

    /**
     * Converts a Garmin Epoch time to a LocalDateTime
     * reference: https://developer.garmin.com/fit/cookbook/datetime/
     * @param date Garmin Epoch time
     * @return LocalDateTime
     */
    fun convertToLocalDateTime(date: Long): LocalDateTime {
        return LocalDateTime.ofEpochSecond(date + GARMIN_OFFSET_S, 0, ZoneOffset.UTC)
    }

    /**
     * Returns the basic Speed for each activity type in m/min
     */
    fun getBaseSpeed(activityType: ActivityType): Double {
        when (activityType) {
            ActivityType.SWIM -> return (5 / 6.0) * 60 // 2 min/100m
            ActivityType.RUN -> return (2 + 7 / 9.0) * 60 // 6 min/km
            ActivityType.BIKE -> return (8 + 2 / 3.0) * 60  // 30km/h
            ActivityType.ROW -> return (3.7) * 60 // 2:15 min/500m
            ActivityType.CROSSCOUNTRYSKIING -> (4 + 4 / 9.0) * 60 // 16km/h
            ActivityType.OTHER -> return 1.0 // no base speed
        }
        return 0.0
    }

    /**
     * Returns the rounded value of the given time with reference to the @const timeSteps
     */
    fun roundTime(time: Int): Int? {
        if (time % timeSteps == 0) {
            return time
        }
        return time + (timeSteps - time % timeSteps)
    }


}