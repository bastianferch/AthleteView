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
    var type: StepType?,
    var durationType: StepDurationType?,
    var durationDistance: Int?,
    var durationDistanceUnit: StepDurationDistanceUnit?,
    var targetType: StepTargetType?,
    var targetFrom: Int?,
    var targetTo: Int?,
    var note: String?
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

    override fun toString(): String{
        return "Step(id=$id, type=$type, durationType=$durationType, durationDistance=$durationDistance, durationDistanceUnit=$durationDistanceUnit, targetType=$targetType, targetFrom=$targetFrom, targetTo=$targetTo, note=$note)"
    }
}