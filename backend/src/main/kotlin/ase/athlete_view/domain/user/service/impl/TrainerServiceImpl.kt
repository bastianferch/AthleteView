package ase.athlete_view.domain.user.service.impl

import ase.athlete_view.common.exception.entity.ForbiddenException
import ase.athlete_view.domain.user.persistence.TrainerRepository
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.service.TrainerService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class TrainerServiceImpl(private val trainerRepository: TrainerRepository) : TrainerService {
    val log = KotlinLogging.logger {}

    override fun getByCode(code: String): Trainer? {
        log.trace { "getByCode $code" }
        return trainerRepository.getTrainerByCode(code)
    }

    override fun acceptAthlete(userDTO: UserDTO, athleteDTO: AthleteDTO) {
        log.trace { "S | acceptAthlete trainer: ${userDTO.email} athlete: ${athleteDTO.email}" }

        val trainer = trainerRepository.findByIdOrNull(userDTO.id!!) ?: throw ForbiddenException("You are not allowed to use this service")
        log.debug { "current ${trainer.athletes}" }
    }

    override fun resetCode(user: UserDTO) {
        log.trace { "S | resetCode $user" }
        val trainer = trainerRepository.findByIdOrNull(user.id!!) ?: throw ForbiddenException("You are not allowed to use this service")
        log.debug { "current code ${trainer.code}" }
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
