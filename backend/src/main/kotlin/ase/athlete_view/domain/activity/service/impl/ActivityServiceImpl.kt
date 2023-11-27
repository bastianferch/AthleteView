package ase.athlete_view.domain.activity.service.impl

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.activity.persistence.IntervalRepository
import ase.athlete_view.domain.activity.persistence.PlannedActivityRepository
import ase.athlete_view.domain.activity.persistence.StepRepository
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.activity.service.validator.ActivityValidator
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ActivityServiceImpl(
    private val plannedActivityRepo: PlannedActivityRepository,
    private val intervalRepo: IntervalRepository,
    private val stepRepo: StepRepository,
    private val validator: ActivityValidator
) : ActivityService {

    private val logger = KotlinLogging.logger {}
    override fun createPlannedActivity(plannedActivity: PlannedActivity): PlannedActivity {
        logger.trace { "S | createPlannedActivity \n $plannedActivity" }
        validator.validateNewPlannedActivity(plannedActivity)
        createInterval(plannedActivity.interval)
        return this.plannedActivityRepo.save(plannedActivity)
    }

    override fun getPlannedActivity(id: Long): PlannedActivity {
        logger.trace { "S | getPlannedActivity $id" }
        return this.plannedActivityRepo.findById(id).orElseThrow { NotFoundException("Planned Activity not found") }
    }

    override fun updatePlannedActivity(id: Long, plannedActivity: PlannedActivity): PlannedActivity {
        logger.trace { "S | updatePlannedActivity $id $plannedActivity" }
        val oldPlannedActivity = this.plannedActivityRepo.findById(id).orElseThrow { NotFoundException("Planned Activity not found") }
        validator.validateEditPlannedActivity(plannedActivity, oldPlannedActivity)
        plannedActivity.interval = createInterval(plannedActivity.interval)
        return this.plannedActivityRepo.save(plannedActivity)
    }


    private fun createInterval(interval: Interval): Interval {
        if (interval.intervals?.isNotEmpty() == true) {
            interval.intervals!!.forEach { createInterval(it) }
        }
        if (interval.step != null) {
            interval.step = createStep(interval.step!!)
        }
        return this.intervalRepo.save(interval)
    }

    private fun createStep(step: Step): Step {
        return this.stepRepo.save(step)
    }

}
