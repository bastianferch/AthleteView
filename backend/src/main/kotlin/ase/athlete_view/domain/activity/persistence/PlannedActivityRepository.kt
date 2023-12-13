package ase.athlete_view.domain.activity.persistence

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlannedActivityRepository : JpaRepository<PlannedActivity, Long> {
    fun findAllByCreatedForId(uid: Long): List<PlannedActivity>
}
