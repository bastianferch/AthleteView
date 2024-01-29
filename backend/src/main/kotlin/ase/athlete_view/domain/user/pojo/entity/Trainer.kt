package ase.athlete_view.domain.user.pojo.entity

import ase.athlete_view.domain.user.pojo.dto.TrainerDTO
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*

@Entity
@DiscriminatorValue("trainer")
class Trainer(
    id: Long?,
    email: String,
    //notifications: List<Notification> = listOf(),
    name: String,
    password: String,
    country: String?,
    zip: String?,

    @Column(unique = true)
    var code: String,

    @OneToMany(cascade = [CascadeType.MERGE, CascadeType.PERSIST], mappedBy = "trainer", fetch = FetchType.LAZY)
    @JsonManagedReference
    var athletes: MutableSet<Athlete>,

    @OneToMany(cascade = [CascadeType.MERGE, CascadeType.PERSIST], fetch = FetchType.LAZY)
    var unacceptedAthletes: MutableSet<Athlete>,
) : User(
    id, email, mutableListOf(), name, password, country, zip, false, mutableSetOf(),
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
            listOf(),
            listOf()
        )
        /*val athletes = athletes.map { it.toAthleteDto(false) }
        trainer.athletes = athletes*/
        return trainer
    }

    fun updateFromDto(dto: TrainerDTO) {
        name = dto.name
        country = dto.country
        zip = dto.zip
        code = dto.code

    }

    override fun getUserType(): String {
        return "trainer"
    }

    override fun toString(): String {
        return "Trainer(id=$id, email='$email', name='$name', country='$country', zip='$zip')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Trainer

        if (code != other.code) return false
        if (athletes != other.athletes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + athletes.hashCode()
        return result
    }


}
