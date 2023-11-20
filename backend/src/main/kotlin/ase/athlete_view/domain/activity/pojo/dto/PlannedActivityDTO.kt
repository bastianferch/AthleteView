package ase.athlete_view.domain.activity.pojo.dto

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import java.util.*

class PlannedActivityDTO(
    var id: Long? = null,
    var type: ActivityType? = null,
    var interval: IntervalDTO? = null,
    var withTrainer: Boolean = false,
    var template: Boolean = false,
    var note: String? = null,
    var date: Date? = null
) {
    fun toEntity(): PlannedActivity { // TODO change activity type
        return PlannedActivity(id, ActivityType.BIKE, interval?.toEntity() ?: null, withTrainer, template, note, date)
    }
    override fun toString(): String {
        return "PlannedActivityDTO(id=$id, type=$type, interval=$interval, withTrainer=$withTrainer, template=$template, note=$note, date=$date)"
    }
}
