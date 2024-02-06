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
package ase.athlete_view.domain.authentication.dto

import ase.athlete_view.domain.user.pojo.entity.Athlete
import java.time.LocalDate

data class AthleteRegistrationDTO
    (
    override var email: String?,
    override var name: String?,
    override var password: String?,
    override var country: String?,
    override var zip: String?,
    var dob: LocalDate?,
    var height: Int?,
    var weight: Int?,
    var code: String?
) :
    RegistrationDTO(email, name, password, country, zip) {

        fun toEntity(): Athlete {
            return Athlete(
                null,
                this.email!!,
                this.name!!,
                this.password!!,
                this.country,
                this.zip,
                this.dob!!,
                this.height!!,
                this.weight!!,
                null,
                null


            )
        }
    override fun toString(): String {
        return "AthleteRegistrationDTO(email=$email, name=$name, country=$country, zip=$zip, dob=$dob, height=$height, weight=$weight, code=$code)"
    }
}
