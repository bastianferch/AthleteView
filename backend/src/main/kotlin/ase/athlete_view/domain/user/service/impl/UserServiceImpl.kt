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
package ase.athlete_view.domain.user.service.impl

import ase.athlete_view.common.exception.entity.ConflictException
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.sanitization.Sanitizer
import ase.athlete_view.domain.authentication.service.AuthValidationService
import ase.athlete_view.domain.user.persistence.PreferencesRepository
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import ase.athlete_view.domain.user.pojo.dto.PreferencesDTO
import ase.athlete_view.domain.user.pojo.dto.TrainerDTO
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Preferences
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val validationService: AuthValidationService,
    private val preferencesRepository: PreferencesRepository,
    private val sanitizer: Sanitizer,
) : UserService {
    val log = KotlinLogging.logger {}

    @Transactional
    override fun save(user: User): User {
        log.trace { "S | save($user)" }
        // store new preferences object if not already present
        if (user.preferences == null) {
            val genericPreferences = Preferences(id = null)
            user.preferences = preferencesRepository.save(genericPreferences)
        }
        return this.userRepository.save(user)
    }

    @Transactional
    override fun saveAll(users: List<User>): List<User> {
        log.trace { "S | saveAll($users)" }
        users.iterator().forEach { user ->
            if (user.preferences == null) {
                user.preferences = preferencesRepository.save(Preferences(id = null))
            }
        }
        return this.userRepository.saveAll(users)
    }

    override fun getByEmail(email: String): User {
        log.trace { "S | getByEmail($email)" }
        return this.userRepository.findByEmail(email) ?: throw NotFoundException("Could not find user by given email")
    }

    override fun getById(id: Long): User {
        log.trace { "S | getById($id)" }
        return this.userRepository.findByIdOrNull(id) ?: throw NotFoundException("Could not find user by given id")
    }

    @Transactional
    override fun updateTrainer(trainerDTO: TrainerDTO) {
        log.trace { "S | updateTrainer($trainerDTO)" }
        val trainer = this.userRepository.findByEmail(trainerDTO.email)
        if (trainer is Trainer) {
            trainer.updateFromDto(sanitizer.sanitizeTrainerDTO(trainerDTO))
            this.validationService.validateUser(trainer)
            this.save(trainer)
        } else {
            throw ConflictException("Could not update trainer by being the athlete")
        }
    }

    @Transactional
    override fun updateAthlete(athleteDTO: AthleteDTO) {
        log.trace { "S | updateAthlete($athleteDTO)" }
        val athlete = this.userRepository.findByEmail(athleteDTO.email)
        if (athlete is Athlete) {
            athlete.updateFromDto(sanitizer.sanitizeAthleteDto(athleteDTO))
            this.validationService.validateUser(athlete)
            this.save(athlete)
        } else {
            throw ConflictException("Could not update athlete by being the trainer")
        }
    }

    override fun getPreferences(userDTO: UserDTO): Preferences? {
        log.trace { "S | getPreferences($userDTO)" }
        if (userDTO.id != null) {
            val user = this.userRepository.findById(userDTO.id!!)
            if (user.isPresent) {
                return user.get().preferences
            }
        }
        return null
    }

    override fun patchPreferences(userDTO: UserDTO, preferencesDTO: PreferencesDTO): Preferences? {
        log.trace { "S | patchPreferences($userDTO, $preferencesDTO)" }
        if (userDTO.id != null) {
            val user = this.userRepository.findById(userDTO.id!!)
            if (user.isPresent) {
                // get user object and its preferences
                val userObj = user.get()
                val oldPreferences = userObj.preferences

                oldPreferences?.emailNotifications = preferencesDTO.emailNotifications
                oldPreferences?.commentNotifications = preferencesDTO.commentNotifications
                oldPreferences?.ratingNotifications = preferencesDTO.ratingNotifications
                oldPreferences?.otherNotifications = preferencesDTO.otherNotifications
                oldPreferences?.shareHealthWithTrainer = preferencesDTO.shareHealthWithTrainer

                val newPreferences = this.preferencesRepository.save(oldPreferences!!)
                userObj.preferences = newPreferences
                this.userRepository.save(userObj)
                return newPreferences
            }
        }
        return null
    }
}
