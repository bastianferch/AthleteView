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
package ase.athlete_view.domain.authentication.service

import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.authentication.dto.AthleteRegistrationDTO
import ase.athlete_view.domain.authentication.dto.TrainerRegistrationDTO
import ase.athlete_view.domain.user.pojo.entity.User

interface AuthValidationService {
    /**
     * Validates the trainer.
     *
     * @throws ValidationException if password, email or name was not presented.
     */
    fun validateTrainerDTO(trainerRegistrationDTO: TrainerRegistrationDTO)
    /**
     * Validates the athlete.
     *
     * @throws ValidationException if password, email or name was not presented.
     */
    fun validateAthleteDTO(athleteRegistrationDTO: AthleteRegistrationDTO)

    /**
     * checks the password -> #validatePassword
     * checks the athlete -> height and weight must be higher than 0. User should be at least 16 y.o.
     * All strings have to be below 255 chars.
     *
     * @throws ValidationException in those cases.
     */
    fun validateUser(user: User)

    /**
     * Validates the password.
     *
     * @throws ValidationException if the password is not provided or has a length less than 8 or bigger than 255.
     */
    fun validatePassword(pass: String)
}
