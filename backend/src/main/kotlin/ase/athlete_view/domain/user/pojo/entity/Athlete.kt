package ase.athlete_view.domain.user.pojo.entity

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDate

@Entity
@DiscriminatorValue("athlete")
class Athlete(
    id: Long?,
    email: String,

    name: String,
    password: String,
    country: String?,
    zip: String?,
    var dob: LocalDate,
    var height: Int, // mm
    var weight: Int, // g
    @ManyToOne()
    @OnDelete(action = OnDeleteAction.SET_NULL)
    var trainer: Trainer?,
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
            weight,
            trainer?.toDto(),
            "",
            "athlete"
        )
    }
    override fun getUserType(): String {
        return "athlete"
    }
}
