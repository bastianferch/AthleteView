package ase.athlete_view.domain.health.pojo.dto

import ase.athlete_view.domain.health.pojo.entity.Health
import ase.athlete_view.domain.user.pojo.entity.User
import java.time.LocalDate

data class HealthDTO (
    var id: Long?,
    var date: LocalDate,
    var avgSteps: Int,
    var avgBPM: Int,
    var avgSleepDuration: Int, // in minutes
){
    fun toEntity(user: User): Health {
        return Health(id, user, date, avgSteps, avgBPM, avgSleepDuration)
    }
}