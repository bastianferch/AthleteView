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

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import ase.athlete_view.domain.activity.pojo.util.Load
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO

import ase.athlete_view.domain.user.pojo.dto.UserDTO
import java.time.LocalDateTime

class PlannedActivityDTO(
    var id: Long? = null,
    var name: String = "",
    var type: ActivityType,
    var interval: IntervalDTO,
    var withTrainer: Boolean = false,
    var template: Boolean = false,
    var note: String? = null,
    var date: LocalDateTime? = null,
    var estimatedDuration: Int? = 60,
    var load: Load? = Load.MEDIUM,
    var createdBy: UserDTO?,  // this property is only used to get the information from the backend to the frontend. The backend should NEVER rely on this field.
    var createdFor: AthleteDTO?,
    var activity: ActivityDTO? = null


) {
    fun toEntity(): PlannedActivity {
        return PlannedActivity(
            id, name, type, interval.toEntity(), withTrainer, template, note, date, estimatedDuration, load, null, createdFor?.toEntity(), activity?.toEntity()
        )
    }

    override fun toString(): String {
        return "PlannedActivityDTO(id=$id, type=$type, \n interval=$interval, withTrainer=$withTrainer, template=$template, note=$note, date=$date, createdBy=$createdBy, createdFor=$createdFor)"
    }
}
