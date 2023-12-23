package ase.athlete_view.domain.user.service.impl

import ase.athlete_view.common.exception.entity.ForbiddenException
import ase.athlete_view.common.exception.entity.NotFoundException
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
class TrainerServiceImpl(private val trainerRepository: TrainerRepository, private val userService: UserService) : TrainerService {
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
        val athlete = trainer.unacceptedAthletes.first { it.id == id}
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
}
