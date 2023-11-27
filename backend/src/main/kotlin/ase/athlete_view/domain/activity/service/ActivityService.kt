package ase.athlete_view.domain.activity.service

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity

interface ActivityService {
    fun createPlannedActivity(plannedActivity: PlannedActivity): PlannedActivity

    fun getPlannedActivity(id: Long): PlannedActivity

    fun updatePlannedActivity(id: Long, plannedActivity: PlannedActivity): PlannedActivity

}
