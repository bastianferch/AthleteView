package ase.athlete_view.domain.time_constraint.persistence

import ase.athlete_view.domain.time_constraint.pojo.entity.TimeConstraint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TimeConstraintRepository : JpaRepository<TimeConstraint, Long> {
}