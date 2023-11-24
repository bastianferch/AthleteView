package ase.athlete_view.domain.user.pojo.entity

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("trainer")
class Trainer(
    id: Long?,
    email: String,
    name: String,
    password: String,
    country: String,
    zip: String,

) : User(
    id, email, name, password, country, zip
)
