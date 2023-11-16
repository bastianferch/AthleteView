package ase.athlete_view.domain.time_constraint.pojo.entity

import ase.athlete_view.domain.time_constraint.pojo.dto.WeeklyTimeConstraintDto
import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@DiscriminatorValue("weekly")
class WeeklyTimeConstraint (

        id: Long?,
        isBlacklist: Boolean,
        user: User,

        @Embedded
        var constraint: TimeFrame

): TimeConstraint(id, isBlacklist, user) {
        override fun toDto(): WeeklyTimeConstraintDto {
                return WeeklyTimeConstraintDto(id, isBlacklist, user, constraint)
        }
        fun toDaily(startOfWeek: LocalDate): DailyTimeConstraint {

                val date = startOfWeek.plusDays(((startOfWeek.dayOfWeek.value - constraint.weekday.value +7) % 7).toLong())
                return DailyTimeConstraint(id, isBlacklist, user, LocalDateTime.of(date, constraint.startTime), LocalDateTime.of(date, constraint.endTime))
        }
}

