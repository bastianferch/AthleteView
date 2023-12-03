package ase.athlete_view.domain.user.service.impl

import ase.athlete_view.domain.user.persistence.TrainerRepository
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.service.TrainerService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class TrainerServiceImpl(private val trainerRepository: TrainerRepository): TrainerService {
    val log = KotlinLogging.logger {}

    override fun getByCode(code: String): Trainer? {
        log.trace { "getByCode $code" }
        return trainerRepository.getTrainerByCode(code)
    }
}
