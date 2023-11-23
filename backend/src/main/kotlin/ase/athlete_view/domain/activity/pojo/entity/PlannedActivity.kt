package ase.athlete_view.domain.activity.pojo.entity

import ase.athlete_view.domain.activity.pojo.dto.PlannedActivityDTO
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.*
import java.util.*

@Entity
class PlannedActivity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    val type: ActivityType,

    @OneToOne
    val interval: Interval,

    val withTrainer: Boolean,

    val template: Boolean,

    val note: String?,

    val date: Date?,

    @ManyToOne
    val createdBy: User,

    @ManyToOne
    val createdFor: Athlete?
) {
    fun toDTO(): PlannedActivityDTO {
        return PlannedActivityDTO(id, type, interval.toDTO(), withTrainer, template, note, date,
            createdBy.toUserDto(), createdFor?.toAthleteDto())
    }
}
