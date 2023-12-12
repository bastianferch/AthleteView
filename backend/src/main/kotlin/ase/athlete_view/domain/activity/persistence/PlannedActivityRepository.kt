package ase.athlete_view.domain.activity.persistence

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface PlannedActivityRepository : JpaRepository<PlannedActivity, Long> {


    @Query("SELECT p FROM PlannedActivity p WHERE p.createdFor.id = :userId AND p.type = :activityType AND DATE(p.date) = :date AND p.activity IS NULL")
    fun findActivitiesByUserIdTypeAndDateWithoutActivity(@Param("userId") userId: Long, @Param("activityType") activityType: ActivityType, @Param("date") date: LocalDate): List<PlannedActivity>

    fun findAllByCreatedForId(uid: Long): List<PlannedActivity>
}
