package ase.athlete_view.domain.activity.pojo.entity

import ase.athlete_view.domain.activity.pojo.util.StepType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
@Entity
class Lap(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?= null,
    var lapNum: Int,
    var time: Int, // in ds (10^-1 s)
    var distance: Int, // in m
    var avgSpeed: Float, // in m/s
    var avgPower: Int, // in W
    var maxPower: Int, // in W
    var avgBpm: Int,
    var maxBpm: Int,
    var avgCadence: Int,
    var maxCadence: Int,
    var stepType: StepType
) {
    override fun toString(): String {
        return "Lap(id=$id, lapNum=$lapNum, time=$time, distance=$distance, avgSpeed=$avgSpeed, avgPower=$avgPower, maxPower=$maxPower, avgBpm=$avgBpm, maxBpm=$maxBpm, avgCadence=$avgCadence, maxCadence=$maxCadence, stepType=$stepType"
    }
}