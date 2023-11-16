package ase.athlete_view.domain.time_constraint.pojo.dto

import ase.athlete_view.domain.time_constraint.pojo.entity.TimeConstraint
import ase.athlete_view.domain.time_constraint.pojo.entity.TimeFrame
import ase.athlete_view.domain.time_constraint.pojo.entity.WeeklyTimeConstraint
import ase.athlete_view.domain.user.pojo.entity.User

class WeeklyTimeConstraintDto (

    id: Long?,
    isBlacklist: Boolean,
    user: User,
    val constraint: TimeFrame

): TimeConstraintDto(id, isBlacklist, user) {
    override fun toEntity(): TimeConstraint {
        return WeeklyTimeConstraint(id, isBlacklist, user, constraint)
    }
}