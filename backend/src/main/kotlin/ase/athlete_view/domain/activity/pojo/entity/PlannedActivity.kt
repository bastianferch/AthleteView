package ase.athlete_view.domain.activity.pojo.entity

import ase.athlete_view.domain.activity.pojo.dto.PlannedActivityDTO
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import ase.athlete_view.domain.activity.pojo.util.Load
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
open class PlannedActivity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long?,

    open val name: String,

    open val type: ActivityType,

    @OneToOne(fetch = FetchType.EAGER)
    open var interval: Interval,

    open val withTrainer: Boolean,

    open val template: Boolean,

    open val note: String?,

    open var date: LocalDateTime?,

    open var estimatedDuration: Int?,

    open var load: Load?,

    @ManyToOne(fetch = FetchType.LAZY)
    open var createdBy: User?,

    @ManyToOne(fetch = FetchType.LAZY)
    open var createdFor: Athlete?,

    @OneToOne(fetch = FetchType.LAZY)
    open var activity: Activity?
) {

    fun toDTO(withoutActivity: Boolean = false): PlannedActivityDTO {
        if (withoutActivity) {
            return PlannedActivityDTO(
                id,
                name,
                type,
                interval.toDTO(),
                withTrainer,
                template,
                note,
                date,
                estimatedDuration,
                load,
                createdBy?.toUserDTO(),
                createdFor?.toAthleteDto(),
                null
            )
        } else {
            return PlannedActivityDTO(
                id,
                name,
                type,
                interval.toDTO(),
                withTrainer,
                template,
                note,
                date,
                estimatedDuration,
                load,
                createdBy?.toUserDTO(),
                createdFor?.toAthleteDto(),
                activity?.toDTO(true)
            )
        }
    }

    fun copyWithNewCreatedForAndWithTrainer(newCreatedFor: Athlete, withTrainer: Boolean): PlannedActivity {
        return PlannedActivity(
                id = null,
                type = this.type,
                interval = this.interval.copy(),
                withTrainer = withTrainer,
                template = false, // Set to non template
                note = this.note,
                date = this.date,
                createdBy = this.createdBy,
                estimatedDuration = this.estimatedDuration,
                name = this.name,
                activity =  this.activity,
                load = this.load,
                createdFor = newCreatedFor // Set to the new Athlete
        )
    }

    override fun toString(): String {
        return "PlannedActivity(id=$id, type=$type, interval=$interval, withTrainer=$withTrainer, template=$template, note=$note, date=$date, createdBy=${createdBy?.id}, createdFor=${createdFor?.id})"
    }

    fun unroll(): List<Step> {
        val steps = mutableListOf<Step>()
        unrollInterval(interval, steps)
        return steps
    }

    private fun unrollInterval(interval: Interval, steps: MutableList<Step>) {
        interval.step?.let { steps.add(it) }
        for (i in 1..interval.repeat) {
            interval.intervals?.forEach { unrollInterval(it, steps) }
        }
    }
}
