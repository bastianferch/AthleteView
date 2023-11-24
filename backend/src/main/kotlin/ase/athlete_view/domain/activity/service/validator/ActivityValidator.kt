package ase.athlete_view.domain.activity.service.validator

import ase.athlete_view.common.exception.entity.ForbiddenException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.time.LocalDate

@Service
@Validated
class ActivityValidator {


    fun validatePlannedActivity(plannedActivity: PlannedActivity, principalID: Long?) {
        val validationErrors: MutableList<String> = ArrayList()


        if (principalID == null) {
            throw(ForbiddenException("You are not logged in"))
        }

        if (plannedActivity.createdBy.id != principalID) {
            throw(ForbiddenException("Your ID does not match the ID of the creator of the activity"))
        }

        if (plannedActivity.template) {
            if (plannedActivity.date != null) {
                validationErrors.add("Date must be null if template is true")
            }
            if (plannedActivity.createdFor != null) {
                validationErrors.add("The athlete, which the activity is for must be null if template is true")
            }
        }
        if (plannedActivity.date != null) {
            if (plannedActivity.date.isBefore(LocalDate.now())) {
                validationErrors.add("Date and time must be in the future")
            }
        }

        if (plannedActivity.interval.intervals?.isNotEmpty() == true) {
            getDepth(plannedActivity.interval.intervals, 0)
            plannedActivity.interval.intervals.forEach { validateInterval(it, validationErrors) }
        }
        if (plannedActivity.interval.step != null) {
            validateStep(plannedActivity.interval.step, validationErrors)
        }
        if (validationErrors.isNotEmpty()) {
            throw ValidationException("Validation of planned Activity failed $validationErrors")
        }
    }

    private fun validateInterval(interval: Interval, validationErrors: MutableList<String>) {
        if (interval.step != null && interval.intervals?.isNotEmpty() == true) {
            validationErrors.add("Step and intervals cannot be set at the same time")
        }
    }

    private fun validateStep(step: Step, validationErrors: MutableList<String>) {

        if (step.durationDistance != null) {
            if (step.durationDistanceUnit == null) {
                validationErrors.add("Duration distance unit must be set if duration distance is set")
            }
            if (step.durationType == null) {
                validationErrors.add("Duration type must be set if duration distance is set")
            }
        }
        if (step.durationType != null) {
            if (step.durationDistance == null) {
                validationErrors.add("Duration distance must be set if duration type is set")
            }
            if (step.durationDistanceUnit == null) {
                validationErrors.add("Duration distance unit must be set if duration type is set")
            }
        }
        if (step.durationDistanceUnit != null) {
            if (step.durationDistance == null) {
                validationErrors.add("Duration distance must be set if duration distance unit is set")
            }
            if (step.durationType == null) {
                validationErrors.add("Duration type must be set if duration distance unit is set")
            }
        }
        if (step.targetType != null) {
            if (step.targetFrom == null) {
                validationErrors.add("Target from must be set if target type is set")
            }
            if (step.targetTo == null) {
                validationErrors.add("Target to must be set if target type is set")
            }
        }
        if (step.targetFrom != null) {
            if (step.targetFrom < 0) {
                validationErrors.add("Target from must be greater than 0")
            }
            if (step.targetTo == null) {
                validationErrors.add("Target to must be set if target from is set")
            }
            if (step.targetTo != null) {
                if (step.targetFrom > step.targetTo) {
                    validationErrors.add("Target from must be smaller than target to")
                }
            }
        }
        if (step.targetTo != null) {
            if (step.targetTo < 0) {
                validationErrors.add("Target to must be greater than 0")
            }
            if (step.targetFrom == null) {
                validationErrors.add("Target from must be set if target to is set")
            }
            if (step.targetType == null) {
                validationErrors.add("Target type must be set if target to is set")
            }
        }

    }

    private fun getDepth(intervals: List<Interval>, i: Int) {
        if (i > 2) {
            throw ValidationException("Depth of at least one of the intervals is too high")
        }
        intervals.forEach {
            if (it.intervals?.isNotEmpty() == true) {
                getDepth(it.intervals, i + 1)
            }
        }
    }
}
