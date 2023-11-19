package ase.athlete_view.domain.time_constraint.pojo.dto

import ase.athlete_view.domain.time_constraint.pojo.entity.DailyTimeConstraint
import ase.athlete_view.domain.time_constraint.pojo.entity.TimeConstraint
import ase.athlete_view.domain.user.pojo.entity.User
import java.time.LocalDateTime

class DailyTimeConstraintDto (

    id: Long?,
    isBlacklist: Boolean,
    user: User?,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime

): TimeConstraintDto(id, isBlacklist, user) {
    override fun toEntity(): TimeConstraint {
        return DailyTimeConstraint(id, isBlacklist, user!!, startTime, endTime)
    }
}