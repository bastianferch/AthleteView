package ase.athlete_view.domain.activity.pojo.entity

import ase.athlete_view.domain.activity.pojo.dto.ActivityDTO
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "activity")
open class Activity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long?,
    @ManyToOne
    open var user: User?,
    open var accuracy: Int,
    open var averageBpm: Int,
    open var maxBpm: Int,
    open var distance: Double,
    open var spentKcal: Int,
    open var cadence: Int,
    open var avgPower: Int,
    open var maxPower: Int,
    open var fitData: String?,
    open var startTime: LocalDateTime?,
    open var endTime: LocalDateTime?,
    @OneToOne(fetch = FetchType.LAZY)
    open var plannedActivity: PlannedActivity?,
    @OneToMany
    open var laps: List<Lap>,
    open var activityType: ActivityType?
) {
    fun toDTO(): ActivityDTO {
        return ActivityDTO(id, accuracy, averageBpm, maxBpm, distance, spentKcal, cadence, avgPower, maxPower, fitData, startTime, endTime,
            plannedActivity?.toDTO(), laps.map { it.toDTO() }, activityType
        )
    }

    override fun toString(): String {
        return "Activity(id=$id, userID=${user?.id}, accuracy=$accuracy, averageBpm=$averageBpm, maxBpm=$maxBpm, distance=$distance, spentKcal=$spentKcal, cadence=$cadence, avgPower=$avgPower, maxPower=$maxPower, fitData=$fitData, startTime=$startTime, endTime=$endTime, plannedActivity = ${plannedActivity?.id})"
    }
}
