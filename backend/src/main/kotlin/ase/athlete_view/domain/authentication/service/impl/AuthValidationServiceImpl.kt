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
package ase.athlete_view.domain.authentication.service.impl

import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.authentication.dto.AthleteRegistrationDTO
import ase.athlete_view.domain.authentication.dto.RegistrationDTO
import ase.athlete_view.domain.authentication.dto.TrainerRegistrationDTO
import ase.athlete_view.domain.authentication.service.AuthValidationService
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AuthValidationServiceImpl: AuthValidationService {

    val log = KotlinLogging.logger {}

    override fun validateTrainerDTO(trainerRegistrationDTO: TrainerRegistrationDTO) {
        log.trace { "S | validateTrainerDTO($trainerRegistrationDTO)" }
        this.validateRegistrationDTO(trainerRegistrationDTO)
    }

    override fun validateAthleteDTO(athleteRegistrationDTO: AthleteRegistrationDTO) {
        log.trace { "S | validateAthleteDTO($athleteRegistrationDTO)" }
        this.validateRegistrationDTO(athleteRegistrationDTO)
    }

    override fun validateUser(user: User) {
        log.trace { "S | validateUser($user)" }
        this.validatePassword(user.password)
        if (user.name.length > 255){
            throw ValidationException("Name length must be below 255 characters.")
        }
        if (user.email.length > 255){
            throw ValidationException("Email length must be below 255 characters.")
        }
        if (user.country != null && user.country?.length!! > 255){
            throw ValidationException("Country length must be below 255 characters.")
        }
        if (user.zip != null && user.zip?.length!! > 255){
            throw ValidationException("Zip length must be below 255 characters.")
        }
        if (user is Athlete) {
            if (user.height <= 0) {
                throw ValidationException("Height should be bigger than 0m")
            }
            if (user.height > 3000) {
                //frontend sends height in mm
                throw ValidationException("Height cannot be bigger than 3m")
            }
            if (user.weight <= 0) {
                throw ValidationException("Weight should be bigger than 0kg")
            }
            if (user.weight > 700000) {
                // frontend sends weight in grams
                throw ValidationException("Weight cannot be bigger than 700kg")
            }
            // ToDo discussionable
            if (user.dob.isAfter(LocalDate.now().minusYears(16))) {
                throw ValidationException("User has to be at least 16 years old to use this app")
            }
        }
    }

    override fun validatePassword(pass: String) {
        log.trace { "S | validatePassword()" }
        if (pass.length < 8) {
            throw ValidationException("Password is too short")
        }
        if (pass.length > 255){
            throw ValidationException("password is too long.")
        }
    }

    private fun validateRegistrationDTO(user: RegistrationDTO) {
        log.trace { "S | validateRegistrationDTO($user)" }
        if (user.password == null || user.email == null || user.name == null) {
            throw ValidationException("User must contain an email, password and the name")
        }
    }

}
