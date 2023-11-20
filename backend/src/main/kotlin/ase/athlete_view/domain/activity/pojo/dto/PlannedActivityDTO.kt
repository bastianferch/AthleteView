package ase.athlete_view.domain.activity.pojo.dto

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import java.util.*

class PlannedActivityDTO(
    var id: Long? = null,
    var type: ActivityType,
    var interval: IntervalDTO? = null,
    var withTrainer: Boolean = false,
    var template: Boolean = false,
    var note: String?,
    var date: Date? = null
) {
    fun toEntity(): PlannedActivity {
        return PlannedActivity(id, type, interval?.toEntity() ?: null, withTrainer, template, note, date)
    }
}
