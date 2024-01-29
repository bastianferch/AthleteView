package ase.athlete_view.domain.time_constraint.pojo.dto

import ase.athlete_view.domain.time_constraint.pojo.entity.TimeConstraint
import ase.athlete_view.domain.time_constraint.pojo.entity.TimeFrame
import ase.athlete_view.domain.time_constraint.pojo.entity.WeeklyTimeConstraint

class WeeklyTimeConstraintDto (

    id: Long?,
    isBlacklist: Boolean,
    title: String,
    val constraint: TimeFrame

): TimeConstraintDto(id, isBlacklist, title) {
    override fun toEntity(): TimeConstraint {
        return WeeklyTimeConstraint(id, isBlacklist, title, null, constraint)
    }
}