package ase.athlete_view.domain.user.service.impl

import ase.athlete_view.domain.user.persistence.TrainerRepository
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.service.TrainerService
import org.springframework.stereotype.Service

@Service
class TrainerServiceImpl(private val trainerRepository: TrainerRepository): TrainerService {
    override fun getByCode(code: String): Trainer? {
        return trainerRepository.getTrainerByCode(code)
    }
}
