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
package ase.athlete_view.domain.activity.pojo.dto

import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import java.time.LocalDateTime

class ActivityDTO(
    var id: Long? = null,
    var accuracy: Int,
    var averageBpm: Int,
    var minBpm: Int,
    var maxBpm: Int,
    var distance: Double,
    var spentKcal: Int,
    var cadence: Int,
    var avgPower: Int,
    var maxPower: Int,
    var fitData: String?,
    var startTime: LocalDateTime?,
    var endTime: LocalDateTime?,
    var plannedActivity: PlannedActivityDTO?,
    var laps : List<LapDTO>,
    var activityType: ActivityType?,
    var comments: List<CommentDTO>,
    var ratingTrainer: Number?,
    var ratingAthlete: Number?,
    var athleteId: Long?
) {
    fun toEntity(): Activity {
        return Activity(id,null, accuracy, averageBpm, minBpm, maxBpm, distance, spentKcal, cadence, avgPower, maxPower, fitData, startTime, endTime, plannedActivity?.toEntity(), laps.map { it.toEntity() } , activityType, mutableListOf(), ratingTrainer ?: 0, ratingAthlete ?: 0)
    }
    override fun toString(): String {
        return "ActivityDTO(id=$id, accuracy=$accuracy, averageBpm=$averageBpm, maxBpm=$maxBpm, distance=$distance, spentKcal=$spentKcal, cadence=$cadence, avgPower=$avgPower, maxPower=$maxPower, fitData=$fitData, startTime=$startTime, endTime=$endTime, athleteId=$athleteId)"
    }
}
