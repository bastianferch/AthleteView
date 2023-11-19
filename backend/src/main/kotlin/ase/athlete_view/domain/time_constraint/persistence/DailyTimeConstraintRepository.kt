package ase.athlete_view.domain.time_constraint.persistence

import ase.athlete_view.domain.time_constraint.pojo.entity.DailyTimeConstraint
import ase.athlete_view.domain.user.pojo.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface DailyTimeConstraintRepository: JpaRepository<DailyTimeConstraint, Long> {

    fun findByUserAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(user: User, startTime: LocalDateTime, endTime: LocalDateTime): List<DailyTimeConstraint>

    fun findByUser(user: User): List<DailyTimeConstraint>
}