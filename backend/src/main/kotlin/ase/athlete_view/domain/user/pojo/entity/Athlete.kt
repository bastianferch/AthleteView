package ase.athlete_view.domain.user.pojo.entity

import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.time.LocalDate

@Entity
@DiscriminatorValue("athlete")
class Athlete(
    id: Long?,
    email: String,

    name: String,
    password: String,
    country: String,
    zip: String,
    var dob: LocalDate,
    var height: Double,
    var weight: Float
) : User(
    id, email, name, password, country, zip
) {

    fun toAthleteDto(): AthleteDTO {
        return AthleteDTO(
            id,
            email,
            name,
            country,
            zip,
            dob,
            height,
            weight
        )
    }
}
