package ase.athlete_view.domain.time_constraint.pojo.dto

import ase.athlete_view.domain.time_constraint.pojo.entity.TimeConstraint
import ase.athlete_view.domain.user.pojo.entity.User

open class TimeConstraintDto (
    val id: Long?,
    val isBlacklist: Boolean,
    var user: User?
) {
    open fun toEntity(): TimeConstraint{
        return TimeConstraint(id, isBlacklist, user!!)
    }
}