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
package ase.athlete_view.domain.user.service

import ase.athlete_view.common.exception.entity.ConflictException
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import ase.athlete_view.domain.user.pojo.dto.PreferencesDTO
import ase.athlete_view.domain.user.pojo.dto.TrainerDTO
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.Preferences
import ase.athlete_view.domain.user.pojo.entity.User

interface UserService {
    /**
     * Simple repository save.
     */
    fun save(user: User): User
    fun saveAll(users: List<User>): List<User>

    /**
     * Simple repository get.
     * @throws NotFoundException if user was not found.
     */
    fun getByEmail(email: String): User

    /**
     * Simple repository get.
     * @throws NotFoundException if user was not found.
     */
    fun getById(id: Long): User

    /**
     * Updates the trainer without id, email and password.
     * @throws ConflictException if user is trying to update the wrong user type (athlete)
     */
    fun updateTrainer(trainerDTO: TrainerDTO)

    /**
     * Updates the athlete without id, email and password.
     * @throws ConflictException if user is trying to update the wrong user type (trainer)
     */
    fun updateAthlete(athleteDTO: AthleteDTO)

    fun getPreferences(userDTO: UserDTO): Preferences?
    fun patchPreferences(userDTO: UserDTO, preferencesDTO: PreferencesDTO): Preferences?
}
