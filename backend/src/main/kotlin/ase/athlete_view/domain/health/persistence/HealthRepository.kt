package ase.athlete_view.domain.health.persistence

import ase.athlete_view.domain.health.pojo.entity.Health
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HealthRepository : JpaRepository<Health, Long> {
}
