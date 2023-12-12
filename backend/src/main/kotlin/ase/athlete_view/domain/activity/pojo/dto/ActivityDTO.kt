
import java.time.LocalDateTime

class ActivityDTO(
    var id: Long? = null,
    var accuracy: Int,
    var averageBpm: Int,
    var maxBpm: Int,
    var distance: Double,
    var spentKcal: Int,
    var cadence: Int,
    var avgPower: Int,
    var maxPower: Int,
    var load: Int,
    var fatigue: Int,
    var fitData: String?,
    var startTime: LocalDateTime?,
    var endTime: LocalDateTime?
) {
    override fun toString(): String {
        return "ActivityDTO(id=$id, accuracy=$accuracy, averageBpm=$averageBpm, maxBpm=$maxBpm, distance=$distance, spentKcal=$spentKcal, cadence=$cadence, avgPower=$avgPower, maxPower=$maxPower, load=$load, fatigue=$fatigue, fitData=$fitData, startTime=$startTime, endTime=$endTime)"
    }
}