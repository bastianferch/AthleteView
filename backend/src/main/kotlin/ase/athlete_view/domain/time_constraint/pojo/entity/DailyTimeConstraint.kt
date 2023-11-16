package ase.athlete_view.domain.time_constraint.pojo.entity

import ase.athlete_view.domain.time_constraint.pojo.dto.DailyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.dto.TimeConstraintDto
import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.validation.Constraint
import java.time.LocalDateTime

@Entity
@DiscriminatorValue("daily")
class DailyTimeConstraint (

        id: Long?,
        isBlacklist: Boolean,
        user: User,

        var startTime: LocalDateTime,
        var endTime: LocalDateTime

): TimeConstraint(id, isBlacklist, user) {
        override fun toDto(): DailyTimeConstraintDto {
                return DailyTimeConstraintDto(id, isBlacklist, user, startTime, endTime)
        }

        fun toWeekly(): WeeklyTimeConstraint {
                return WeeklyTimeConstraint(id, isBlacklist, user, TimeFrame(startTime.dayOfWeek, startTime.toLocalTime(), endTime.toLocalTime()))
        }
}



