package ase.athlete_view.domain.time_constraint.pojo.dto

import ase.athlete_view.domain.time_constraint.pojo.entity.DailyTimeConstraint
import ase.athlete_view.domain.time_constraint.pojo.entity.TimeConstraint
import java.time.LocalDateTime

class DailyTimeConstraintDto (

    id: Long?,
    isBlacklist: Boolean,
    title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime

): TimeConstraintDto(id, isBlacklist, title) {
    override fun toEntity(): TimeConstraint {
        return DailyTimeConstraint(id, isBlacklist, title, null, startTime, endTime)
    }
}