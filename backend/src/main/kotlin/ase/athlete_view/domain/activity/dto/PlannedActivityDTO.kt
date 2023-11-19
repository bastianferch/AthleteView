package ase.athlete_view.domain.activity.dto

class PlannedActivityDTO {
    var id: Long? = null
    var type: String? = null
    var interval: IntervalDTO? = null
    var withTrainer: Boolean? = null
    var template: Boolean? = null
    var note: String? = null
    var date: String? = null
}