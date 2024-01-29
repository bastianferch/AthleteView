package ase.athlete_view.domain.activity.persistence

import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.user.pojo.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ActivityRepository: JpaRepository<Activity, Long> {
    fun save(activity: Activity): Activity
    fun findActivitiesByUserId(uid: Long): List<Activity>

    @Query("SELECT a FROM Activity a WHERE a.user.id = :uid AND a.startTime >= :start AND a.endTime <= :end")
    fun findActivitiesByUserAndDateRange(
        @Param("uid") userId: Long,
        @Param("start") startTime: LocalDateTime,
        @Param("end") endTime: LocalDateTime
    ): List<Activity>
}
