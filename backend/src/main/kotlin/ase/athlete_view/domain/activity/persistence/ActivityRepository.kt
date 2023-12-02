package ase.athlete_view.domain.activity.persistence

import ase.athlete_view.domain.activity.pojo.entity.Activity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ActivityRepository: JpaRepository<Activity, String> {
    fun save(activity: Activity): Activity
}
