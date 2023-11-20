package ase.athlete_view.domain.time_constraint.pojo.dto

import ase.athlete_view.domain.time_constraint.pojo.entity.DailyTimeConstraint
import ase.athlete_view.domain.time_constraint.pojo.entity.TimeConstraint
import ase.athlete_view.domain.user.pojo.entity.User
import java.time.LocalDateTime

class DailyTimeConstraintDto (

    id: Long?,
    isBlacklist: Boolean,
    title: String,
    user: User?,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime

): TimeConstraintDto(id, isBlacklist, title, user) {
    override fun toEntity(): TimeConstraint {
        return DailyTimeConstraint(id, isBlacklist, title, user!!, startTime, endTime)
    }
}