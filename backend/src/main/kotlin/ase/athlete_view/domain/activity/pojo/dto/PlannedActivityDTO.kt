package ase.athlete_view.domain.activity.pojo.dto

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO

import ase.athlete_view.domain.user.pojo.dto.UserDto
import java.time.LocalDate

class PlannedActivityDTO(
    var id: Long? = null,
    var type: ActivityType,
    var interval: IntervalDTO,
    var withTrainer: Boolean = false,
    var template: Boolean = false,
    var note: String? = null,
    var date: LocalDate? = null,
    var createdBy: UserDto?, // TODO this property is only used to get the information from the backend to the frontend. The backend should NEVER rely on this field.
    var createdFor: AthleteDTO?

) {
    fun toEntity(): PlannedActivity {
        return PlannedActivity(id, type, interval.toEntity(), withTrainer, template, note, date, null, createdFor?.toEntity())
    }

    override fun toString(): String {
        return "PlannedActivityDTO(id=$id, type=$type, \n interval=$interval, withTrainer=$withTrainer, template=$template, note=$note, date=$date, createdBy=$createdBy, createdFor=$createdFor)"
    }
}
