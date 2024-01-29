package ase.athlete_view.domain.time_constraint.pojo.dto

import ase.athlete_view.domain.time_constraint.pojo.entity.TimeConstraint

open class TimeConstraintDto (
    val id: Long?,
    val isBlacklist: Boolean,
    val title: String,
) {
    open fun toEntity(): TimeConstraint{
        return TimeConstraint(id, isBlacklist, title, user = null)
    }

    override fun toString(): String {
        return "TimeConstraintDto(id=$id, isBlacklist=$isBlacklist, title=$title)"
    }
}