package ase.athlete_view.domain.activity.pojo.dto

import ase.athlete_view.domain.activity.pojo.entity.Lap
import ase.athlete_view.domain.activity.pojo.util.StepType

class LapDTO(
    var id: Long? = null,
    var lapNum: Int,
    var time: Int?, // in ds
    var distance: Int?, // in m
    var avgSpeed: Float?, // in m/s
    var avgPower: Int?, // in W
    var maxPower: Int?, // in W
    var avgBpm: Int?,
    var maxBpm: Int?,
    var avgCadence: Int?,
    var maxCadence: Int?,
    var stepType: StepType?
) {

    fun toEntity(): Lap {
        return Lap(
            id, lapNum, time, distance, avgSpeed, avgPower, maxPower, avgBpm, maxBpm, avgCadence,
            maxCadence, stepType
        )
    }

}