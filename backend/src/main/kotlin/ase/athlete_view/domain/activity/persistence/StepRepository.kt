package ase.athlete_view.domain.activity.persistence

import ase.athlete_view.domain.activity.pojo.entity.Step
import org.springframework.data.jpa.repository.JpaRepository

interface StepRepository : JpaRepository<Step, Long> {
}