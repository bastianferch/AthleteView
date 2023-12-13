package ase.athlete_view.domain.user.pojo.entity

import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
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

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "trainer_id")
    @JsonBackReference
    var trainer: Trainer?,
) : User(
    id, email, name, password, country, zip, true, mutableSetOf(),
) {

    fun toAthleteDto(include_trainer: Boolean = true): AthleteDTO {
        return if (include_trainer) {
            AthleteDTO(
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
        } else {
            AthleteDTO(
                    id,
                    email,
                    name,
                    country,
                    zip,
                    dob,
                    height,
                    weight,
                    null,
                    "",
                    "athlete"
            )
        }
    }
    override fun getUserType(): String {
        return "athlete"
    }

    override fun toString(): String {
        return "Athlete(User=${super.toString()}, dob=$dob, height=$height, weight=$weight, trainer=$trainer)"
    }
}
