package ase.athlete_view.domain.health.pojo.dto

import java.time.LocalDate

data class HealthDTO (
    var id: Long?,
    var date: LocalDate,
    var avgSteps: Int,
    var avgBPM: Int,
    var avgSleepDuration: Int, // in minutes
){}