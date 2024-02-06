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
package ase.athlete_view.domain.activity.pojo.entity

import ase.athlete_view.domain.activity.pojo.dto.StepDTO
import ase.athlete_view.domain.activity.pojo.util.StepDurationUnit
import ase.athlete_view.domain.activity.pojo.util.StepDurationType
import ase.athlete_view.domain.activity.pojo.util.StepTargetType
import ase.athlete_view.domain.activity.pojo.util.StepType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Step(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    var type: StepType?,
    var durationType: StepDurationType?,
    var durationDistance: Int?,
    var durationDistanceUnit: StepDurationUnit?,
    var targetType: StepTargetType?,
    var targetFrom: Int?,
    var targetTo: Int?,
    var note: String?
) {
    fun toDTO(): StepDTO {
        return StepDTO(
            id,
            type,
            durationType,
            durationDistance,
            durationDistanceUnit,
            targetType,
            targetFrom,
            targetTo,
            note
        )
    }

    fun copy(): Step {
        return Step(
                null,
                this.type,
                this.durationType,
                this.durationDistance,
                this.durationDistanceUnit,
                this.targetType,
                this.targetFrom,
                this.targetTo,
                this.note
        )
    }

    override fun toString(): String{
        return "Step(id=$id, type=$type, durationType=$durationType, durationDistance=$durationDistance, durationDistanceUnit=$durationDistanceUnit, targetType=$targetType, targetFrom=$targetFrom, targetTo=$targetTo, note=$note)"
    }
}
