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
package ase.athlete_view.domain.user.pojo.entity

import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.CascadeType
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDate

@Entity
@DiscriminatorValue("athlete")
class Athlete(
    id: Long?,
    email: String,
    //notifications: List<Notification> = listOf(),

    name: String,
    password: String,
    country: String?,
    zip: String?,
    var dob: LocalDate,
    var height: Int, // mm
    var weight: Int, // g

    @ManyToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH])
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "trainer_id")
    @JsonBackReference
    var trainer: Trainer?,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JsonBackReference
    var trainerToBe: Trainer?
) : User(
    id, email, mutableListOf(), name, password, country, zip, false, mutableSetOf(),
) {

    fun updateFromDto(dto: AthleteDTO) {
        name = dto.name
        country = dto.country
        zip = dto.zip
        dob = dto.dob
        height = dto.height
        weight = dto.weight
    }

    fun toAthleteDto(includeTrainer: Boolean = true): AthleteDTO {
        return if (includeTrainer) {
            AthleteDTO(
                    id,
                    email,
                    name,
                    country,
                    zip,
                    dob,
                    height,
                    weight,
                    trainer?.toDto(),
                    trainerToBe?.toDto(),
                    "",
                    "athlete"
            )
        } else {
            AthleteDTO(
                    id,
                    email,
                    name,
                    country,
                    zip,
                    dob,
                    height,
                    weight,
                    null,
                    null,
                    "",
                    "athlete"
            )
        }
    }
    override fun getUserType(): String {
        return "athlete"
    }

    override fun toString(): String {
        return "Athlete(User=${super.toString()}, dob=$dob, height=$height, weight=$weight, trainer=$trainer)"
    }
}
