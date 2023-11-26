package ase.athlete_view.domain.user.pojo.entity

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
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
    val dob: LocalDate,
    val height: Int, // mm
    val weight: Int, // g
    @ManyToOne()
    @OnDelete(action = OnDeleteAction.SET_NULL)
    var trainer: Trainer?

) : User(
    id, email, name, password, country, zip
)
