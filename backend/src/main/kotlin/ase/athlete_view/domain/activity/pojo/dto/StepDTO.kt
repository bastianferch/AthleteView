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
    fun toEntity(): Step {
        return Step(id, type, durationType, durationDistance, durationDistanceUnit, targetType, targetFrom, targetTo, note)
    }

    override fun toString(): String {
        return "StepDTO(id=$id, type=$type, durationType=$durationType, durationDistance=$durationDistance, durationDistanceUnit=$durationDistanceUnit, targetType=$targetType, targetFrom=$targetFrom, targetTo=$targetTo, note=$note)"
    }
}
