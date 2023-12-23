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
class TrainerServiceImpl(private val trainerRepository: TrainerRepository, private val userService: UserService, private val mailService: MailService) : TrainerService {
    val log = KotlinLogging.logger {}

    override fun getByCode(code: String): Trainer? {
        log.trace { "getByCode $code" }
        return trainerRepository.getTrainerByCode(code)
    }

    override fun acceptAthlete(userDTO: UserDTO, id: Long) {
        log.trace { "S | acceptAthlete trainer: ${userDTO.email} athlete: $id" }
        val trainer = trainerRepository.findByIdOrNull(userDTO.id!!) ?: throw ForbiddenException("You are not allowed to use this service")
        if (trainer.unacceptedAthletes.none { it.id == id }) {
            if (trainer.athletes.any { it.id == id }) {
                throw ForbiddenException("You are already training this athlete")
            } else {
                throw NotFoundException("Athlete not found") // TODO check FE why user is logged out
            }
        }
        val athlete = trainer.unacceptedAthletes.first { it.id == id }
        trainer.unacceptedAthletes.remove(athlete)
        trainer.athletes.add(athlete)
        athlete.trainer = trainer
        this.userService.saveAll(listOf(trainer, athlete))
    }

    override fun resetCode(user: UserDTO) {
        log.trace { "S | resetCode $user" }
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
        log.trace { "S | inviteAthletes $id $emailList" }
        val trainer = trainerRepository.findByIdOrNull(id) ?: throw ForbiddenException("You are not allowed to use this service")
        val invalidEmail = mutableListOf<String>()
        for(email in emailList){
            // TODO discuss with security engineer to prevent abuse
            try {
                mailService.sendSimpleMail(Email(email,
                    "You have been invited to Athlete View by ${trainer.name}.\nPlease register at http://localhost:4200/register/${trainer.code}", // TODO replace url for production
                    "Invitation to Athlete View"))
            } catch (e: ValidationException){
                invalidEmail.add(email)
            }
        }
        if (invalidEmail.isNotEmpty()) {
            if (invalidEmail.size == emailList.size)
                throw ValidationException("Invalid email addresses: ${invalidEmail.joinToString(", ")}")
            else {
                throw ValidationException("Invalid email addresses: ${invalidEmail.joinToString(", ")}. Other emails have been sent.")
            }
        }
    }
}
