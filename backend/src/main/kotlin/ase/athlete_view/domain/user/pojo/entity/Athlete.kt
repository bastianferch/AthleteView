package ase.athlete_view.domain.user.pojo.entity

import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.CascadeType
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
    //notifications: List<Notification> = listOf(),

    name: String,
    password: String,
    country: String?,
    zip: String?,
    var dob: LocalDate,
    var height: Int, // mm
    var weight: Int, // g

    @ManyToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH])
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "trainer_id")
    @JsonBackReference
    var trainer: Trainer?,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JsonBackReference
    var trainerToBe: Trainer?
) : User(
    id, email, mutableListOf(), name, password, country, zip, true, mutableSetOf(),
) {

    fun toAthleteDto(includeTrainer: Boolean = true): AthleteDTO {
        return if (includeTrainer) {
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
                    trainerToBe?.toDto(),
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
