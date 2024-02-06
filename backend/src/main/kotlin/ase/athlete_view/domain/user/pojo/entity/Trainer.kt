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

import ase.athlete_view.domain.user.pojo.dto.TrainerDTO
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*

@Entity
@DiscriminatorValue("trainer")
class Trainer(
    id: Long?,
    email: String,
    //notifications: List<Notification> = listOf(),
    name: String,
    password: String,
    country: String?,
    zip: String?,

    @Column(unique = true)
    var code: String,

    @OneToMany(cascade = [CascadeType.MERGE, CascadeType.PERSIST], mappedBy = "trainer", fetch = FetchType.LAZY)
    @JsonManagedReference
    var athletes: MutableSet<Athlete>,

    @OneToMany(cascade = [CascadeType.MERGE, CascadeType.PERSIST], fetch = FetchType.LAZY)
    var unacceptedAthletes: MutableSet<Athlete>,
) : User(
    id, email, mutableListOf(), name, password, country, zip, false, mutableSetOf(),
) {
    fun toDto(): TrainerDTO {
        val trainer =  TrainerDTO(
            id,
            email,
            name,
            country,
            zip,
            code,
            "",
            "trainer",
            listOf(),
            listOf()
        )
        /*val athletes = athletes.map { it.toAthleteDto(false) }
        trainer.athletes = athletes*/
        return trainer
    }

    fun updateFromDto(dto: TrainerDTO) {
        name = dto.name
        country = dto.country
        zip = dto.zip
        code = dto.code

    }

    override fun getUserType(): String {
        return "trainer"
    }

    override fun toString(): String {
        return "Trainer(id=$id, email='$email', name='$name', country='$country', zip='$zip')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Trainer

        if (code != other.code) return false
        if (athletes != other.athletes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + athletes.hashCode()
        return result
    }


}
