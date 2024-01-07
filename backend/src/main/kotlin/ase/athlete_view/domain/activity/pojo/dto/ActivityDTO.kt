package ase.athlete_view.domain.activity.pojo.dto

import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import java.time.LocalDateTime

class ActivityDTO(
    var id: Long? = null,
    var accuracy: Int,
    var averageBpm: Int,
    var minBpm: Int,
    var maxBpm: Int,
    var distance: Double,
    var spentKcal: Int,
    var cadence: Int,
    var avgPower: Int,
    var maxPower: Int,
    var fitData: String?,
    var startTime: LocalDateTime?,
    var endTime: LocalDateTime?,
    var plannedActivity: PlannedActivityDTO?,
    var laps : List<LapDTO>,
    var activityType: ActivityType?,
    var comments: List<CommentDTO>,
    var ratingTrainer: Number?,
    var ratingAthlete: Number?,
) {
    fun toEntity(): Activity {
        return Activity(id,null, accuracy, averageBpm, minBpm, maxBpm, distance, spentKcal, cadence, avgPower, maxPower, fitData, startTime, endTime, plannedActivity?.toEntity(), laps.map { it.toEntity() } , activityType, mutableListOf(), ratingTrainer ?: 0, ratingAthlete ?: 0)
    }
    override fun toString(): String {
        return "ActivityDTO(id=$id, accuracy=$accuracy, averageBpm=$averageBpm, maxBpm=$maxBpm, distance=$distance, spentKcal=$spentKcal, cadence=$cadence, avgPower=$avgPower, maxPower=$maxPower, fitData=$fitData, startTime=$startTime, endTime=$endTime)"
    }
}