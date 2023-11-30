package ase.athlete_view.domain.user.pojo.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity
@DiscriminatorValue("trainer")
class Trainer(
    id: Long?,
    email: String,
    name: String,
    password: String,
    country: String?,
    zip: String?,
    @Column(unique = true)
    val code: String,
    @OneToMany(cascade = [CascadeType.MERGE, CascadeType.PERSIST])
    var athletes: List<Athlete> = ArrayList()

) : User(
    id, email, name, password, country, zip
)