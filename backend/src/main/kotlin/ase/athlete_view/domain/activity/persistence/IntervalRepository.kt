package ase.athlete_view.domain.activity.persistence

import ase.athlete_view.domain.activity.pojo.entity.Interval
import org.springframework.data.jpa.repository.JpaRepository

interface IntervalRepository : JpaRepository<Interval, Long> {
}