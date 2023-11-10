package ase.athlete_view.domain.user.pojo.entity

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.time.LocalDate

@Entity
@DiscriminatorValue("athlete")
class Athlete(
    override var id: Long?,
    override var email: String,

    override var name: String,
    override var password: String,
    override var country: String,
    override var zip: String,
    var dob: LocalDate,
    var height: Double,
    var weight: Float
) : User(
    id, email, name, password, country, zip
)
