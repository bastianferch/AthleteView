package ase.athlete_view.domain.activity.service.impl

import ase.athlete_view.domain.activity.persistence.IntervalRepository
import ase.athlete_view.domain.activity.persistence.PlannedActivityRepository
import ase.athlete_view.domain.activity.persistence.StepRepository
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.activity.service.validator.ActivityValidator
import ase.athlete_view.domain.user.pojo.dto.UserDto
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class ActivityServiceImpl(private val plannedActivityRepo: PlannedActivityRepository,
                          private val intervalRepo: IntervalRepository,
                          private val stepRepo: StepRepository,
                          private val validator: ActivityValidator
) : ActivityService {
    override fun createPlannedActivity(plannedActivity: PlannedActivity, principal: UserDto): PlannedActivity {
        validator.validatePlannedActivity(plannedActivity, principal.id)
        createInterval(plannedActivity.interval)
        return this.plannedActivityRepo.save(plannedActivity)
    }

    fun createInterval(interval: Interval): Interval {
        if(interval.intervals?.isNotEmpty() == true){
            interval.intervals.forEach { createInterval(it) }
        }
        if(interval.step != null){
            createStep(interval.step)
        }
        return this.intervalRepo.save(interval)
    }

    private fun createStep(step: Step): Step {
        stepRepo.findAll()
        return this.stepRepo.save(step)
    }
}
