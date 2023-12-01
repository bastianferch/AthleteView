package ase.athlete_view.domain.activity.pojo.entity

import ase.athlete_view.domain.activity.pojo.dto.PlannedActivityDTO
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class PlannedActivity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    val type: ActivityType,

    @OneToOne
    var interval: Interval,

    val withTrainer: Boolean,

    val template: Boolean,

    val note: String?,

    val date: LocalDateTime?,

    @ManyToOne
    var createdBy: User?,

    @ManyToOne
    val createdFor: Athlete?
) {
    fun toDTO(): PlannedActivityDTO {
        return PlannedActivityDTO(id, type, interval.toDTO(), withTrainer, template, note, date,
            createdBy?.toUserDto(), createdFor?.toAthleteDto())
    }

    override fun toString(): String {
        return "PlannedActivity(id=$id, type=$type, interval=$interval, withTrainer=$withTrainer, template=$template, note=$note, date=$date, createdBy=$createdBy, createdFor=$createdFor)"
    }
}
