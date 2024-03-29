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

import ase.athlete_view.domain.health.pojo.dto.HealthDTO
import ase.athlete_view.domain.health.pojo.entity.Health
import ase.athlete_view.domain.user.pojo.entity.User
import java.time.LocalDate

class HealthCreator {
    companion object {
        val DEFAULT_DATE_1: LocalDate = LocalDate.of(2020, 1, 1)
        val DEFAULT_DATE_2: LocalDate = LocalDate.of(2020, 1, 2)
        val DEFAULT_DATE_3: LocalDate = LocalDate.of(2020, 1, 3)
        val DEFAULT_DATE_4: LocalDate = LocalDate.of(2020, 1, 4)
        val DEFAULT_DATE_5: LocalDate = LocalDate.of(2020, 1, 5)
        val DEFAULT_DATE_6: LocalDate = LocalDate.of(2020, 1, 6)
        val DEFAULT_DATE_7: LocalDate = LocalDate.of(2020, 1, 7)
        const val DEFAULT_AVG_STEPS_1: Int = 5000
        const val DEFAULT_AVG_BPM_1: Int = 70
        const val DEFAULT_AVG_SLEEP_DURATION_1: Int = 485
        const val DEFAULT_AVG_STEPS_2: Int = 10000
        const val DEFAULT_AVG_BPM_2: Int = 80
        const val DEFAULT_AVG_SLEEP_DURATION_2: Int = 500

        fun getDefaultGoodHealth(user: User, date: LocalDate): Health {
            return Health(
                id = null,
                user = user,
                date = date,
                avgSteps = 10000,
                avgBPM = 80,
                avgSleepDuration = 8 * 60
            )
        }

        fun getDefaultMehHealth(user: User, date: LocalDate): Health {
            return Health(
                id = null,
                user = user,
                date = date,
                avgSteps = 1500,
                avgBPM = 60,
                avgSleepDuration = 2 * 60
            )
        }

        fun defaultHealthForOneWeek_1(user: User): List<Health> {
            return listOf(
                Health(
                    id = null,
                    date = DEFAULT_DATE_1,
                    avgSteps = DEFAULT_AVG_STEPS_1,
                    avgBPM = DEFAULT_AVG_BPM_1,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_1,
                    user = user
                ),
                Health(
                    id = null,
                    date = DEFAULT_DATE_2,
                    avgSteps = DEFAULT_AVG_STEPS_1,
                    avgBPM = DEFAULT_AVG_BPM_1,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_1,
                    user = user
                ),
                Health(
                    id = null,
                    date = DEFAULT_DATE_3,
                    avgSteps = DEFAULT_AVG_STEPS_1,
                    avgBPM = DEFAULT_AVG_BPM_1,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_1,
                    user = user
                ),
                Health(
                    id = null,
                    date = DEFAULT_DATE_4,
                    avgSteps = DEFAULT_AVG_STEPS_1,
                    avgBPM = DEFAULT_AVG_BPM_1,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_1,
                    user = user
                ),
                Health(
                    id = null,
                    date = DEFAULT_DATE_5,
                    avgSteps = DEFAULT_AVG_STEPS_1,
                    avgBPM = DEFAULT_AVG_BPM_1,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_1,
                    user = user
                ),
                Health(
                    id = null,
                    date = DEFAULT_DATE_6,
                    avgSteps = DEFAULT_AVG_STEPS_1,
                    avgBPM = DEFAULT_AVG_BPM_1,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_1,
                    user = user
                ),
                Health(
                    id = null,
                    date = DEFAULT_DATE_7,
                    avgSteps = DEFAULT_AVG_STEPS_1,
                    avgBPM = DEFAULT_AVG_BPM_1,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_1,
                    user = user
                ),
            )
        }

        fun getDefaultHealthForOneWeekDto1(): Array<HealthDTO> {
            return arrayOf(
                HealthDTO(
                    id = null,
                    date = DEFAULT_DATE_1,
                    avgSteps = DEFAULT_AVG_STEPS_1,
                    avgBPM = DEFAULT_AVG_BPM_1,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_1
                ),
                HealthDTO(
                    id = null,
                    date = DEFAULT_DATE_2,
                    avgSteps = DEFAULT_AVG_STEPS_1,
                    avgBPM = DEFAULT_AVG_BPM_1,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_1
                ),
                HealthDTO(
                    id = null,
                    date = DEFAULT_DATE_3,
                    avgSteps = DEFAULT_AVG_STEPS_1,
                    avgBPM = DEFAULT_AVG_BPM_1,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_1
                ),
                HealthDTO(
                    id = null,
                    date = DEFAULT_DATE_4,
                    avgSteps = DEFAULT_AVG_STEPS_1,
                    avgBPM = DEFAULT_AVG_BPM_1,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_1
                ),
                HealthDTO(
                    id = null,
                    date = DEFAULT_DATE_5,
                    avgSteps = DEFAULT_AVG_STEPS_1,
                    avgBPM = DEFAULT_AVG_BPM_1,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_1
                ),
                HealthDTO(
                    id = null,
                    date = DEFAULT_DATE_6,
                    avgSteps = DEFAULT_AVG_STEPS_1,
                    avgBPM = DEFAULT_AVG_BPM_1,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_1
                ),
                HealthDTO(
                    id = null,
                    date = DEFAULT_DATE_7,
                    avgSteps = DEFAULT_AVG_STEPS_1,
                    avgBPM = DEFAULT_AVG_BPM_1,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_1
                ),
            )
        }

        fun defaultHealthForOneWeekDto2(): Array<HealthDTO> {
            return arrayOf(
                HealthDTO(
                    id = null,
                    date = DEFAULT_DATE_2,
                    avgSteps = DEFAULT_AVG_STEPS_2,
                    avgBPM = DEFAULT_AVG_BPM_2,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_2
                ),
                HealthDTO(
                    id = null,
                    date = DEFAULT_DATE_2,
                    avgSteps = DEFAULT_AVG_STEPS_2,
                    avgBPM = DEFAULT_AVG_BPM_2,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_2
                ),
                HealthDTO(
                    id = null,
                    date = DEFAULT_DATE_3,
                    avgSteps = DEFAULT_AVG_STEPS_2,
                    avgBPM = DEFAULT_AVG_BPM_2,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_2
                ),
                HealthDTO(
                    id = null,
                    date = DEFAULT_DATE_4,
                    avgSteps = DEFAULT_AVG_STEPS_2,
                    avgBPM = DEFAULT_AVG_BPM_2,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_2
                ),
                HealthDTO(
                    id = null,
                    date = DEFAULT_DATE_5,
                    avgSteps = DEFAULT_AVG_STEPS_2,
                    avgBPM = DEFAULT_AVG_BPM_2,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_2
                ),
                HealthDTO(
                    id = null,
                    date = DEFAULT_DATE_6,
                    avgSteps = DEFAULT_AVG_STEPS_2,
                    avgBPM = DEFAULT_AVG_BPM_2,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_2
                ),
                HealthDTO(
                    id = null,
                    date = DEFAULT_DATE_7,
                    avgSteps = DEFAULT_AVG_STEPS_2,
                    avgBPM = DEFAULT_AVG_BPM_2,
                    avgSleepDuration = DEFAULT_AVG_SLEEP_DURATION_2
                ),
            )
        }
    }
}
