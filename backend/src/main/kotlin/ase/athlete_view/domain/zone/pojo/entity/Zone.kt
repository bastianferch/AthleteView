package ase.athlete_view.domain.zone.pojo.entity

import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.zone.pojo.dto.ZoneDto
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
class Zone (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,
    val name: String,
    var fromBPM: Int,
    var toBPM: Int,

    @ManyToOne
    var user: Athlete?
)

fun Zone.toDTO(): ZoneDto {
    return ZoneDto(id,name,fromBPM,toBPM)
}
