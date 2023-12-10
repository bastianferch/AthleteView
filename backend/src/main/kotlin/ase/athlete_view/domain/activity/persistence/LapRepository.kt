package ase.athlete_view.domain.activity.persistence

import ase.athlete_view.domain.activity.pojo.entity.Lap
import org.springframework.data.jpa.repository.JpaRepository

interface LapRepository: JpaRepository<Lap, Long> {
}