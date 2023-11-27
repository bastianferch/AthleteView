package ase.athlete_view.domain.user.pojo.dto

import ase.athlete_view.domain.user.pojo.entity.Athlete
import java.time.LocalDate

class AthleteDTO (
    var id: Long?,
    var email: String,
    var name: String,
    var country: String?,
    var zip: String?,
    var dob: LocalDate,
    var height: Int,
    var weight: Int,
    var trainer: TrainerDTO?
){
    fun toEntity(): Athlete {
        return Athlete(
            id,
            email,
            name,
            "",
            country,
            zip,
            dob,
            height,
            weight,
            trainer?.toEntity()
        )
    }
}