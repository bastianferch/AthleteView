package ase.athlete_view.domain.health.persistence

import ase.athlete_view.domain.health.pojo.entity.Health
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface HealthRepository : JpaRepository<Health, Long> {
    fun findByDate(date: LocalDate): Optional<Health>


    fun findByUser_Id(id: Long): List<Health>
}
