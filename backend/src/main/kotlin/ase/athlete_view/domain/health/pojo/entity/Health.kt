package ase.athlete_view.domain.health.pojo.entity

import ase.athlete_view.domain.health.pojo.dto.HealthDTO
import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.proxy.HibernateProxy
import java.time.LocalDate

@Entity
data class Health(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var user: User,

    var date: LocalDate,
    var avgSteps: Int,
    var avgBPM: Int,
    var avgSleepDuration: Int, // in minutes
) {
    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , date = $date , avgSteps = $avgSteps , avgBPM = $avgBPM , avgSleepDuration = $avgSleepDuration )"
    }

    fun toDTO(): HealthDTO {
        return HealthDTO(id, date, avgSteps, avgBPM, avgSleepDuration)
    }
}
