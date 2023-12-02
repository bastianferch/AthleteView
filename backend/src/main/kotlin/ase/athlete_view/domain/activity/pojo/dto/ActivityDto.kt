package ase.athlete_view.domain.activity.pojo.dto

class ActivityDto(
    private var id: String? = null,
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
    private var fitData: String
) {
}