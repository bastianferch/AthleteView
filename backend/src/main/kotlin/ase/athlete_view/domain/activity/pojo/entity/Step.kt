package ase.athlete_view.domain.activity.pojo.entity

import ase.athlete_view.domain.activity.pojo.dto.StepDTO
import ase.athlete_view.domain.activity.pojo.util.StepDurationDistanceUnit
import ase.athlete_view.domain.activity.pojo.util.StepDurationType
import ase.athlete_view.domain.activity.pojo.util.StepTargetType
import ase.athlete_view.domain.activity.pojo.util.StepType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Step(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    val type: StepType?,
    val durationType: StepDurationType?,
    val durationDistance: Int?,
    val durationDistanceUnit: StepDurationDistanceUnit?,
    val targetType: StepTargetType?,
    val targetFrom: Int?,
    val targetTo: Int?,
    val note: String?
) {
    fun toDTO(): StepDTO {
        return StepDTO(
            id,
            type,
            durationType,
            durationDistance,
            durationDistanceUnit,
            targetType,
            targetFrom,
            targetTo,
            note
        )

    }
}
