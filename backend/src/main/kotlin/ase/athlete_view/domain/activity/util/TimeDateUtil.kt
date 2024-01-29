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
     * Returns the basic Speed for each activity type in s/km
     */
    fun getBaseSpeed(activityType: ActivityType): Double {
        return when (activityType) {
            ActivityType.SWIM -> 1200.0  // 2 min/100m
            ActivityType.RUN -> 360.0 // 6 min/km
            ActivityType.BIKE -> 144.0  // 25km/h
            ActivityType.ROW -> 300.0 // 2:30 min/500m
            ActivityType.CROSSCOUNTRYSKIING -> 300.0 // 12km/h
            ActivityType.OTHER -> 1.0 // no base speed
        }
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