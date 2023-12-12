package ase.athlete_view.util

import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.*
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import java.time.DateTimeException
import java.time.LocalDateTime

class ActivityCreator {
        companion object {
            fun getDefaultActivity(): Activity {
                return Activity(
                    1L,
                    UserCreator.getAthlete(),
                    0,
                    0,
                    0,
                    0.0,
                    0,
                    0,
                    0,
                    0,
                    "",
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(120),
                    null,
                    listOf()
                )
            }

            fun getDefaultPlannedActivity(trainer: Trainer, date: LocalDateTime?, athlete: Athlete?): PlannedActivity {
                return PlannedActivity(
                        null, ActivityType.RUN, getDefaultInterval(), false, false,
                        "Sample planned activity", date ?: LocalDateTime.now().plusDays(5), trainer, athlete, null
                )
            }

            fun getDefaultStep(): Step {
                return Step(
                        null, StepType.ACTIVE, StepDurationType.DISTANCE, 30, StepDurationUnit.KM,
                        StepTargetType.CADENCE, 100, 200, "Sample step note"
                )
            }


            fun getDefaultInterval(): Interval {
                return Interval(null, 1, listOf(Interval(null, 2, listOf(Interval(null, 1, null, getDefaultStep())), null)), null)
            }
        }
}