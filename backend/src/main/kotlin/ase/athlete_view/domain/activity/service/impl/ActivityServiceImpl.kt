package ase.athlete_view.domain.activity.service.impl

import ase.athlete_view.domain.activity.persistence.PlannedActivityRepository
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.service.ActivityService
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service

@Service
@RequiredArgsConstructor
class ActivityServiceImpl(private val plannedActivityRepo: PlannedActivityRepository): ActivityService {
    override fun createPlannedActivity(plannedActivity: PlannedActivity): PlannedActivity {
        return this.plannedActivityRepo.save(plannedActivity)

    }
}
