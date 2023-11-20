package ase.athlete_view.domain.activity.pojo.dto

import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.StepDurationDistanceUnit
import ase.athlete_view.domain.activity.pojo.util.StepDurationType
import ase.athlete_view.domain.activity.pojo.util.StepTargetType
import ase.athlete_view.domain.activity.pojo.util.StepType

class StepDTO(
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
    fun toEntity(): Step { // TODO change type
        return Step(id, StepType.COOLDOWN, StepDurationType.DISTANCE, durationDistance, durationDistanceUnit, targetType, targetFrom, targetTo, note)
    }
}
