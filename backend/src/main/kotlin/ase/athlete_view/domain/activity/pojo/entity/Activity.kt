package ase.athlete_view.domain.activity.pojo.entity

import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.*

@Entity
@Table(name = "activity")
class Activity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long?,
    @OneToOne
    private var user: User,
    private var accuracy: Int,
    private var averageBpm: Int,
    private var maxBpm: Int,
    private var distance: Double,
    private var spentKcal: Int,
    private var cadence: Int,
    private var avgPower: Int,
    private var maxPower: Int,
    private var load: Int,
    private var fatigue: Int,
    private var fitData: String?,
) {
    override fun toString(): String {
        return "Activity(id=$id, accuracy=$accuracy, averageBpm=$averageBpm, maxBpm=$maxBpm, distance=$distance, spentKcal=$spentKcal, cadence=$cadence, avgPower=$avgPower, maxPower=$maxPower, load=$load, fatigue=$fatigue, fitData=$fitData)"
    }
}
