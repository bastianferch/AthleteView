package ase.athlete_view.domain.time_constraint.persistence

import ase.athlete_view.domain.time_constraint.pojo.entity.WeeklyTimeConstraint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WeeklyTimeConstraintRepository: JpaRepository<WeeklyTimeConstraint, Long> {
}