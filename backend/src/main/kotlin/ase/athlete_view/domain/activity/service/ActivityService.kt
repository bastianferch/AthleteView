package ase.athlete_view.domain.activity.service

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.user.pojo.dto.UserDto

interface ActivityService {
    fun createPlannedActivity(plannedActivity: PlannedActivity, principal: UserDto): PlannedActivity

}
