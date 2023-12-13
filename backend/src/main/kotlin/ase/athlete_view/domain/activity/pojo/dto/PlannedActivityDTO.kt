package ase.athlete_view.domain.activity.pojo.dto

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import ase.athlete_view.domain.activity.pojo.util.Load
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO

import ase.athlete_view.domain.user.pojo.dto.UserDTO
import java.time.LocalDateTime

class PlannedActivityDTO(
    var id: Long? = null,
    var name: String,
    var type: ActivityType,
    var interval: IntervalDTO,
    var withTrainer: Boolean = false,
    var template: Boolean = false,
    var note: String? = null,
    var date: LocalDateTime? = null,
    var estimatedDuration: Int? = 60,
    var load: Load? = Load.MEDIUM,
    var createdBy: UserDTO?,  // this property is only used to get the information from the backend to the frontend. The backend should NEVER rely on this field.
    var createdFor: AthleteDTO?,
    var activity: ActivityDTO? = null


) {
    fun toEntity(): PlannedActivity {
        return PlannedActivity(
            id, name, type, interval.toEntity(), withTrainer, template, note, date, estimatedDuration, load, null, createdFor?.toEntity(), activity?.toEntity()
        )
    }

    override fun toString(): String {
        return "PlannedActivityDTO(id=$id, type=$type, \n interval=$interval, withTrainer=$withTrainer, template=$template, note=$note, date=$date, createdBy=$createdBy, createdFor=$createdFor)"
    }
}
