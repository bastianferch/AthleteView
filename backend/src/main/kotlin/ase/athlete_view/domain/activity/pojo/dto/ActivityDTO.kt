package ase.athlete_view.domain.activity.pojo.dto

import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.user.pojo.dto.UserDTO

class ActivityDTO(
    private var id: Long? = null,
    private var user: UserDTO?,
    private var accuracy: Int,
    private var averageBpm: Int,
    private var maxBpm: Int,
    private var distance: Double,
    private var spentKcal: Int,
    private var cadence: Int,
    private var avgPower: Int,
    private var maxPower: Int,
    private var load: Int,
    private var fatigue: Int,
    private var fitData: String,
    private var plannedActivity: PlannedActivityDTO?,
    private var laps : List<LapDTO>
) {
    fun toEntity():Activity{
        return Activity(id, null, accuracy, averageBpm, maxBpm, distance, spentKcal, cadence, avgPower, maxPower, load, fatigue, fitData, plannedActivity?.toEntity(), laps.map { it.toEntity() })
    }
}
