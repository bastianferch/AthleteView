package ase.athlete_view.util

import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.*
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import java.time.LocalDateTime

class ActivityCreator {
        companion object {
            fun getDefaultActivity(): Activity {
                return Activity(
                    1L,
                    UserCreator.getAthlete(null),
                    0,
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
                    listOf(),
                    null
                )
            }

            fun getDefaultPlannedActivity(trainer: Trainer, date: LocalDateTime?, athlete: Athlete?): PlannedActivity {
                return PlannedActivity(
                        null, "test activity", ActivityType.RUN, getDefaultInterval(), false, false,
                        "Sample planned activity", date ?: LocalDateTime.now().plusDays(5), 60, Load.MEDIUM, trainer, athlete, null
                )
            }


            fun getTemplatePlannedActivity(trainer: Trainer): PlannedActivity {
                return PlannedActivity(null,"default",ActivityType.RUN, getDefaultInterval(),true,true,"Sample template activity",
                        null,3,Load.LOW,trainer,null,null);
            }

            fun get7Times1KmPlannedActivity(createdBy: User, createdFor: Athlete?):PlannedActivity{
                return PlannedActivity(1," 7x(1km P:1')", ActivityType.RUN,
                    Interval(null,1, listOf(
                        Interval(null,1,null,
                            Step(null,StepType.WARMUP, StepDurationType.LAPBUTTON,null,null,null,null,null,null)),
                        Interval(null,7, listOf(
                            Interval(null,1,null,
                                Step(null,StepType.ACTIVE, StepDurationType.DISTANCE,1,StepDurationUnit.KM,StepTargetType.PACE,240,260,"")),
                            Interval(null,1,null,
                                Step(null,StepType.RECOVERY,StepDurationType.TIME,2,StepDurationUnit.MIN,null,null,null,null))),
                            null),
                        Interval(null,1,null,
                            Step(null,StepType.COOLDOWN,StepDurationType.LAPBUTTON,null,null,null,null,null,null))),null)
                    ,
                    false,false, "", LocalDateTime.of(2023,9,30,12,10), 60,Load.MEDIUM,createdBy,createdFor, null)


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