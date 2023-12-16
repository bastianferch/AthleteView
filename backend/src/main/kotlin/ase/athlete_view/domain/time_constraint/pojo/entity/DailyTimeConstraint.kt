package ase.athlete_view.domain.time_constraint.pojo.entity

import ase.athlete_view.domain.time_constraint.pojo.dto.DailyTimeConstraintDto
import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.time.LocalDateTime

@Entity
@DiscriminatorValue("daily")
class DailyTimeConstraint (

        id: Long?,
        isBlacklist: Boolean,
        title: String,
        user: User?,

        var startTime: LocalDateTime,
        var endTime: LocalDateTime

): TimeConstraint(id, isBlacklist, title, user) {
        override fun toDto(): DailyTimeConstraintDto {
                return DailyTimeConstraintDto(id, isBlacklist, title, startTime, endTime)
        }

        fun toWeekly(): WeeklyTimeConstraint {
                return WeeklyTimeConstraint(id, isBlacklist, title, user, TimeFrame(startTime.dayOfWeek, startTime.toLocalTime(), endTime.toLocalTime()))
        }
}



