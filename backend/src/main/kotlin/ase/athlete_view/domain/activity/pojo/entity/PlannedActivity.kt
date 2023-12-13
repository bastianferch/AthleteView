package ase.athlete_view.domain.activity.pojo.entity

import ase.athlete_view.domain.activity.pojo.dto.PlannedActivityDTO
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
open class PlannedActivity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long?,

    open val type: ActivityType,

    @OneToOne
    open var interval: Interval,

    open val withTrainer: Boolean,

    open val template: Boolean,

    open val note: String?,

    open val date: LocalDateTime?,

    @ManyToOne(fetch = FetchType.LAZY)
    open var createdBy: User?,

    @ManyToOne(fetch = FetchType.LAZY)
    open var createdFor: Athlete?
) {
    fun toDTO(): PlannedActivityDTO {
        return PlannedActivityDTO(id, type, interval.toDTO(), withTrainer, template, note, date,
            createdBy?.toUserDTO(), createdFor?.toAthleteDto())
    }

    override fun toString(): String {
        return "PlannedActivity(id=$id, type=$type, interval=$interval, withTrainer=$withTrainer, template=$template, note=$note, date=$date, createdBy=${createdBy?.id}, createdFor=${createdFor?.id})"
    }
}
