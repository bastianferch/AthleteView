package ase.athlete_view.domain.activity.pojo.entity

import ase.athlete_view.domain.activity.pojo.dto.ActivityDTO
import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "activity")
class Activity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long?,
    @ManyToOne
    private var user: User,
    private var accuracy: Int,
    private var averageBpm: Int,
    private var maxBpm: Int,
    private var distance: Double,
    private var spentKcal: Int,
    private var cadence: Int,
    private var avgPower: Int,
    private var maxPower: Int,
    private var fitData: String?,
    private var startTime: LocalDateTime?,
    private var endTime: LocalDateTime?,
    @OneToOne(fetch = FetchType.LAZY)
    private var plannedActivity: PlannedActivity?,
    @OneToMany
    private var laps : List<Lap>
) {
    fun toDTO(): ActivityDTO {
        return ActivityDTO(id, accuracy, averageBpm, maxBpm, distance, spentKcal, cadence, avgPower, maxPower, load, fatigue, fitData, startTime, endTime)
    }

    override fun toString(): String {
        return "Activity(id=$id, userID=${user.id}, accuracy=$accuracy, averageBpm=$averageBpm, maxBpm=$maxBpm, distance=$distance, spentKcal=$spentKcal, cadence=$cadence, avgPower=$avgPower, maxPower=$maxPower, load=$load, fatigue=$fatigue, fitData=$fitData, startTime=$startTime, endTime=$endTime)"
    }
}
