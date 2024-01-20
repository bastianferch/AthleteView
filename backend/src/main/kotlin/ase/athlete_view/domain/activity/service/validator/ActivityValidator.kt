package ase.athlete_view.domain.activity.service.validator

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.activity.pojo.entity.Comment
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.StepDurationType
import io.github.oshai.kotlinlogging.KotlinLogging
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.time.LocalDateTime

@Service
@Validated
class ActivityValidator {
    val log = KotlinLogging.logger {}
    val commentTextLength = 1000 // characters

    fun validateNewPlannedActivity(plannedActivity: PlannedActivity, user: User, isCsp: Boolean = false) {
        log.trace { "S | Validating new planned activity $plannedActivity" }
        val validationErrors: MutableList<String> = ArrayList()

        if (plannedActivity.name.isBlank()) {
            validationErrors.add("Name must not be blank")
        } else if (plannedActivity.name.length > 255) {
            validationErrors.add("Name must be shorter than 255 characters")
        }


        if (user is Athlete) {
            if (plannedActivity.createdFor != null && plannedActivity.createdFor != user) {// TODO fix error which occurs during updating of planned activity, no specific action needed
                validationErrors.add("Athletes can only create Activities for themselves")
            }
            if (plannedActivity.createdFor == null && !plannedActivity.template) {
                plannedActivity.createdFor = user
            }
            if (plannedActivity.withTrainer) {
                validationErrors.add("Athletes cannot create activities with trainer presence")
            }
        }

        if (user is Trainer) {
            if(!isCsp){
                if (plannedActivity.createdFor != null && plannedActivity.date == null) {
                    validationErrors.add("Date must be set if activity is created for an athlete")
                }
            }

            if (plannedActivity.createdFor == null && plannedActivity.date != null) {
                validationErrors.add("Athlete must be set if date is set")
            }

                if (!plannedActivity.template && plannedActivity.createdFor != null) {
                    val athletes = user.athletes
                    var isForAthleteOfTrainer = false
                    for (athlete in athletes) {
                        if (plannedActivity.createdFor!!.id == athlete.id) {
                            isForAthleteOfTrainer = true
                            break
                        }
                    }
                    if (!isForAthleteOfTrainer) {
                        validationErrors.add("Trainers can only create Activities for their Athletes or templates")
                    }
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
            if (plannedActivity.date!!.isBefore(LocalDateTime.now())) {
                validationErrors.add("Date and time must be in the future")
            }
        }

        if (plannedActivity.interval.intervals?.isNotEmpty() == true) {
            getDepth(plannedActivity.interval.intervals!!, 0)
            validateInterval(plannedActivity.interval, validationErrors)
        }
        if (plannedActivity.note != null) {
            if (plannedActivity.note!!.length > 255) {
                validationErrors.add("Note must be shorter than 255 characters")
            }
        }
        if (validationErrors.isNotEmpty()) {
            throw ValidationException("Validation of planned Activity failed $validationErrors")
        }
    }

    fun validateEditPlannedActivity(plannedActivity: PlannedActivity, oldPlannedActivity: PlannedActivity, user: User) {
        log.trace { "S | Validating planned activity for edit $plannedActivity" }
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

    fun validateComment(comment: Comment) {
        if (comment.text.length > commentTextLength) {
            throw ValidationException("Comments must have length <= 1000 characters")
        }
    }

    fun validateRating(rating: Int) {
        val r = rating.toInt()
        if (r > 5 || r < 0) {
            throw ValidationException("Rating must be between 0 and 5")
        }
    }

    private fun validateInterval(interval: Interval, validationErrors: MutableList<String>) {
        if (interval.intervals != null) {
            if (interval.step != null && interval.intervals!!.isNotEmpty()) {
                validationErrors.add("Step and intervals cannot be set at the same time")
            }
            interval.intervals!!.forEach { validateInterval(it, validationErrors) }
        }
        if (interval.step != null) {
            validateStep(interval.step!!, validationErrors)
        }
    }

    private fun validateStep(step: Step, validationErrors: MutableList<String>) {
        if (step.durationDistance != null) {
            if (step.durationDistanceUnit == null) {
                validationErrors.add("Duration distance unit must be set if duration distance is set for ${step.type}")
            }
            if (step.durationType == null) {
                validationErrors.add("Duration type must be set if duration distance is set for ${step.type}")
            }
        }
        if (step.durationType != null && step.durationType != StepDurationType.LAPBUTTON) {
            if (step.durationDistance == null) {
                validationErrors.add("Duration distance must be set if duration type is set for ${step.type}")
            }
            if (step.durationDistanceUnit == null) {
                validationErrors.add("Duration distance unit must be set if duration type is set for ${step.type}")
            }
        }
        if (step.durationDistanceUnit != null) {
            if (step.durationDistance == null) {
                validationErrors.add("Duration distance must be set if duration distance unit is set for ${step.type}")
            }
            if (step.durationType == null) {
                validationErrors.add("Duration type must be set if duration distance unit is set for ${step.type}")
            }
        }
        if (step.targetType != null) {
            if (step.targetFrom == null) {
                validationErrors.add("Target from must be set if target type is set for ${step.type}")
            }
            if (step.targetTo == null) {
                validationErrors.add("Target to must be set if target type is set for ${step.type}")
            }
        }
        if (step.targetFrom != null) {
            if (step.targetFrom!! < 0) {
                validationErrors.add("Target from must be greater than 0 for ${step.type}")
            }
            if (step.targetTo == null) {
                validationErrors.add("Target to must be set if target from is set for ${step.type}")
            }
            if (step.targetTo != null) {
                if (step.targetFrom!! > step.targetTo!!) {
                    validationErrors.add("Target from must be smaller than target to for ${step.type}")
                }
            }
        }
        if (step.targetTo != null) {
            if (step.targetTo!! < 0) {
                validationErrors.add("Target to must be greater than 0 for ${step.type}")
            }
            if (step.targetFrom == null) {
                validationErrors.add("Target from must be set if target to is set for ${step.type}")
            }
            if (step.targetType == null) {
                validationErrors.add("Target type must be set if target to is set for ${step.type}")
            }
        }

        if (step.note != null) {
            if (step.note!!.length > 255) {
                validationErrors.add("Note must be shorter than 255 characters for ${step.type} ")
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
