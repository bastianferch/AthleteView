/*-
 * #%L
 * athlete_view
 * %%
 * Copyright (C) 2023 - 2024 TU Wien INSO ASE GROUP 5 WS2023
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package ase.athlete_view.util

import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.*
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import java.time.LocalDate
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
                    null,
                )
            }

            fun getHealthyForDefaultAthleteActivity(user: User, date: LocalDate): Activity{
                val activity = this.getDefaultActivity()
                activity.id = null
                activity.user = user
                activity.startTime = date.atTime(15, 0, 0)
                activity.endTime = date.atTime(16, 0, 0)
                activity.averageBpm = 130
                activity.maxBpm = 180
                return activity
            }

            fun getMehForDefaultAthleteActivity(user: User, date: LocalDate): Activity{
                val activity = this.getDefaultActivity()
                activity.id = null
                activity.user = user
                activity.startTime = date.atTime(16, 0, 0)
                activity.endTime = date.atTime(17, 0, 0)
                activity.averageBpm = 40
                activity.maxBpm = 40
                return activity
            }

            fun getDefaultPlannedActivity(trainer: Trainer, date: LocalDateTime?, athlete: Athlete?): PlannedActivity {
                return PlannedActivity(
                        null, "test activity", ActivityType.RUN, getDefaultInterval(), false, false,
                        "Sample planned activity", date ?: LocalDateTime.now().plusDays(5), 60, Load.MEDIUM, trainer, athlete, null
                )
            }


            fun getTemplatePlannedActivity(trainer: Trainer): PlannedActivity {
                return PlannedActivity(null,"default",ActivityType.RUN, getDefaultInterval(),true,true,"Sample template activity",
                        null,3,Load.LOW,trainer,null,null)
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
