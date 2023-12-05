package ase.athlete_view.domain.activity.service.validator

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import io.github.oshai.kotlinlogging.KotlinLogging
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.transaction.Transactional
import org.hibernate.Hibernate
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.time.LocalDateTime

@Service
@Validated
class ActivityValidator {
    val log = KotlinLogging.logger {}

    fun validateNewPlannedActivity(plannedActivity: PlannedActivity, user: User) {
        log.trace { "Validating new planned activity $plannedActivity" }
        val validationErrors: MutableList<String> = ArrayList()

        if (user is Athlete) {
            if (plannedActivity.createdFor != null && plannedActivity.createdFor != user) {
                validationErrors.add("Athletes can only create Activities for themselves")
            }
            if (plannedActivity.withTrainer == true) {
                validationErrors.add("Athletes cannot create activities with trainer presence")
            }
        }



        if (user is Trainer && !plannedActivity.template && plannedActivity.createdFor != null) {
            val athletes = user.athletes
            var isForAthleteOfTrainer = false
            for (athlete in athletes) {
                if (plannedActivity.createdFor.id == athlete.id) {
                    isForAthleteOfTrainer = true
                    break
                }
            }
            if (!isForAthleteOfTrainer) {
                validationErrors.add("Trainers can only create Activities for their Athletes or templates")
            }
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
            if (plannedActivity.date.isBefore(LocalDateTime.now())) {
                validationErrors.add("Date and time must be in the future")
            }
        }

        if (plannedActivity.interval.intervals?.isNotEmpty() == true) {
            getDepth(plannedActivity.interval.intervals!!, 0)
            validateInterval(plannedActivity.interval, validationErrors)
            plannedActivity.interval.intervals!!.forEach { validateInterval(it, validationErrors) }
        }
        if (plannedActivity.interval.step != null) {
            validateStep(plannedActivity.interval.step!!, validationErrors)
        }
        if (plannedActivity.note != null) {
            if (plannedActivity.note.length > 255) {
                validationErrors.add("Note must be shorter than 255 characters")
            }
        }
        if (validationErrors.isNotEmpty()) {
            throw ValidationException("Validation of planned Activity failed $validationErrors")
        }
    }

    fun validateEditPlannedActivity(plannedActivity: PlannedActivity, oldPlannedActivity: PlannedActivity, user: User) {
        log.trace { "Validating planned activity for edit $plannedActivity" }
        // check if the user is allowed to update this activity
        if (user is Athlete) {
            // if the logged-in user is an Athlete, they can only edit their own activities
            if (user.activities.none { it.id == oldPlannedActivity.id }) {
                throw ValidationException("Athletes can only edit their own Activities")
            }
        } else if (user is Trainer) {
            val isOwnTemplate = user.activities.any { it.id == oldPlannedActivity.id }
            var isForAthleteOfTrainer = false
            for (athlete in user.athletes) {
                if (athlete.activities.any { it.id == oldPlannedActivity.id }) {
                    isForAthleteOfTrainer = true
                }
            }
            if (!isOwnTemplate && !isForAthleteOfTrainer) {
                throw ValidationException("Trainers can only edit Activities of their Athletes and their own templates")
            }
        } else {
            throw ValidationException("Only Trainers and Athletes can edit Activities")
        }

        if (plannedActivity.id != oldPlannedActivity.id) {
            throw NotFoundException("Planned Activity not found")
        }

        validateNewPlannedActivity(plannedActivity, user)
    }

    private fun validateInterval(interval: Interval, validationErrors: MutableList<String>) {
        if (interval.intervals != null) {
            if (interval.step != null && interval.intervals!!.isNotEmpty() == true) {
                validationErrors.add("Step and intervals cannot be set at the same time")
            }
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
            if (step.targetFrom!! < 0) {
                validationErrors.add("Target from must be greater than 0")
            }
            if (step.targetTo == null) {
                validationErrors.add("Target to must be set if target from is set")
            }
            if (step.targetTo != null) {
                if (step.targetFrom!! > step.targetTo!!) {
                    validationErrors.add("Target from must be smaller than target to")
                }
            }
        }
        if (step.targetTo != null) {
            if (step.targetTo!! < 0) {
                validationErrors.add("Target to must be greater than 0")
            }
            if (step.targetFrom == null) {
                validationErrors.add("Target from must be set if target to is set")
            }
            if (step.targetType == null) {
                validationErrors.add("Target type must be set if target to is set")
            }
        }

        if (step.note != null) {
            if (step.note!!.length > 255) {
                validationErrors.add("Note must be shorter than 255 characters")
            }
        }

    }

    private fun getDepth(intervals: List<Interval>, i: Int) {
        if (i > 2) {
            throw ValidationException("Depth of at least one of the intervals is too high")
        }
        intervals.forEach {
            if (it.intervals?.isNotEmpty() == true) {
                getDepth(it.intervals!!, i + 1)
            }
        }
    }
}
