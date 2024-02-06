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

import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.StepDurationUnit
import ase.athlete_view.domain.activity.pojo.util.StepDurationType
import ase.athlete_view.domain.activity.pojo.util.StepTargetType
import ase.athlete_view.domain.activity.pojo.util.StepType

class StepDTO(
    val id: Long?,
    val type: StepType?,
    val durationType: StepDurationType?,
    val duration: Int?,
    val durationUnit: StepDurationUnit?,
    val targetType: StepTargetType?,
    val targetFrom: Int?,
    val targetTo: Int?,
    val note: String?
) {
    fun toEntity(): Step {
        return Step(id, type, durationType, duration, durationUnit, targetType, targetFrom, targetTo, note)
    }

    override fun toString(): String {
        return "StepDTO(id=$id, type=$type, durationType=$durationType, durationDistance=$duration, durationDistanceUnit=$durationUnit, targetType=$targetType, targetFrom=$targetFrom, targetTo=$targetTo, note=$note)"
    }
}
