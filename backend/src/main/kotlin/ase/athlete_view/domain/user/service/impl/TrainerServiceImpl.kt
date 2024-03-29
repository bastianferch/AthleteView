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

import ase.athlete_view.common.exception.entity.ForbiddenException
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.mail.pojo.entity.Email
import ase.athlete_view.domain.mail.service.MailService
import ase.athlete_view.domain.user.persistence.TrainerRepository
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.service.TrainerService
import ase.athlete_view.domain.user.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class TrainerServiceImpl(private val trainerRepository: TrainerRepository, private val userService: UserService, private val mailService: MailService) :
    TrainerService {
    val log = KotlinLogging.logger {}

    override fun getByCode(code: String): Trainer? {
        log.trace { "S | getByCode($code)" }
        return trainerRepository.getTrainerByCode(code)
    }

    override fun acceptAthlete(userDTO: UserDTO, id: Long) {
        log.trace { "S | acceptAthlete($userDTO, $id)" }
        val trainer = trainerRepository.findByIdOrNull(userDTO.id!!) ?: throw ForbiddenException("You are not allowed to use this service")
        if (trainer.unacceptedAthletes.none { it.id == id }) {
            if (trainer.athletes.any { it.id == id }) {
                throw ForbiddenException("You are already training this athlete")
            } else {
                throw NotFoundException("Athlete not found")
            }
        }
        val athlete = trainer.unacceptedAthletes.first { it.id == id }
        trainer.unacceptedAthletes.remove(athlete)
        trainer.athletes.add(athlete)
        athlete.trainer = trainer
        this.userService.saveAll(listOf(trainer, athlete))
    }

    override fun resetCode(user: UserDTO) {
        log.trace { "S | resetCode($user)" }
        val trainer = trainerRepository.findByIdOrNull(user.id!!) ?: throw ForbiddenException("You are not allowed to use this service")
        while (true) {
            val code = UUID.randomUUID().toString().substring(0, 5).replace('-', Random().nextInt().toChar())
            if (getByCode(code) == null) {
                trainer.code = code
                break
            }
        }
        trainerRepository.save(trainer)
    }

    override fun inviteAthletes(id: Long, emailList: List<String>) {
        log.trace { "S | inviteAthletes($id, $emailList)" }
        val trainer = trainerRepository.findByIdOrNull(id) ?: throw ForbiddenException("You are not allowed to use this service")
        val invalidEmail = mutableListOf<String>()
        val alreadyTrainingEmail = mutableListOf<String>()
        val emailRegex = Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
        var exceptionMessage = ""
        for (email in emailList) {
            if (trainer.athletes.any { it.email == email } || trainer.unacceptedAthletes.any { it.email == email }) {
                alreadyTrainingEmail.add(email)
                continue
            }
            if (!emailRegex.matches(email)) {
                invalidEmail.add(email)
                continue
            }
            try {
                mailService.sendSimpleMail(
                    Email(
                        email,
                        "You have been invited to Athlete View by ${trainer.name}.\nPlease register at http://localhost:4200/auth/registration/${trainer.code}", // TODO replace url for production
                        "Invitation to Athlete View"
                    )
                )
            } catch (e: ValidationException) {
                invalidEmail.add(email)
            }
        }
        if (invalidEmail.isNotEmpty()) {
            exceptionMessage += if (invalidEmail.size == emailList.size)
                "Invalid email addresses: ${invalidEmail.joinToString(", ")}"
            else {
                "Invalid email addresses: ${invalidEmail.joinToString(", ")}. Other emails have been sent."
            }
        }

        if (alreadyTrainingEmail.isNotEmpty()) {
            exceptionMessage += "You are already training or have to accept these athletes ${alreadyTrainingEmail.joinToString(", ")}"
        }

        if (exceptionMessage != "")
            throw ValidationException(exceptionMessage)
    }
}
