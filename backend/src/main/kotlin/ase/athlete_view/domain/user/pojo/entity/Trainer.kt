package ase.athlete_view.domain.user.pojo.entity

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("trainer")
class Trainer(
    override var id: Long?,
    override var email: String,

    override var name: String,
    override var password: String,
    override var country: String,
    override var zip: String,

) : User(
    id, email, name, password, country, zip
)
