package ase.athlete_view.domain.user.pojo.entity

import ase.athlete_view.domain.user.pojo.dto.TrainerDTO
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
) {
    fun toDto(): TrainerDTO {
        val trainer =  TrainerDTO(
            id,
            email,
            name,
            country,
            zip,
            code,
            "",
            "trainer",
            listOf()
        )
        val athletes = athletes.map { it.toAthleteDto() }
        trainer.athletes = athletes
        return trainer
    }

    override fun getUserType(): String {
        return "trainer"
    }

    override fun toString(): String {
        return "Trainer(id=$id, email='$email', name='$name', country='$country', zip='$zip')"
    }
}
