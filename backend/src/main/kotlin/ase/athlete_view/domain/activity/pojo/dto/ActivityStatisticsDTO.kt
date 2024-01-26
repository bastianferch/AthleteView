package ase.athlete_view.domain.activity.pojo.dto

import java.time.LocalDateTime

open class ActivityStatisticsDTO(
        open var timestamp: LocalDateTime,
        open var bpm: Int = 0,
        open var speed: Float = 0.0f,
        open var cadence: Short = 0,
        open var power: Int = 0,
        open var altitude: Float = 0.0f,
)