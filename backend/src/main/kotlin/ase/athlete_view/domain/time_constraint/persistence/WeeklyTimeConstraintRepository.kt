package ase.athlete_view.domain.time_constraint.persistence

import ase.athlete_view.domain.time_constraint.pojo.entity.WeeklyTimeConstraint
import ase.athlete_view.domain.user.pojo.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WeeklyTimeConstraintRepository: JpaRepository<WeeklyTimeConstraint, Long> {

    fun findByUser(user: User): List<WeeklyTimeConstraint>
}