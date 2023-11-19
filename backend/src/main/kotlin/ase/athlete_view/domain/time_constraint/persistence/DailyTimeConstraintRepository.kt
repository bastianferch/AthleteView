package ase.athlete_view.domain.time_constraint.persistence

import ase.athlete_view.domain.time_constraint.pojo.entity.DailyTimeConstraint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface DailyTimeConstraintRepository: JpaRepository<DailyTimeConstraint, Long> {

    fun findAllByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(startTime: LocalDateTime, endTime: LocalDateTime): List<DailyTimeConstraint>
}