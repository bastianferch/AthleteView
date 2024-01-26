package ase.athlete_view.domain.user.service.impl

import ase.athlete_view.common.exception.entity.ForbiddenException
import ase.athlete_view.common.exception.entity.NotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ase.athlete_view.domain.user.persistence.AthleteRepository
import ase.athlete_view.domain.user.persistence.TrainerRepository
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.service.AthleteService
import io.github.oshai.kotlinlogging.KotlinLogging


@Service
class AthleteServiceImpl(val athleteRepository: AthleteRepository, val trainerRepo: TrainerRepository) : AthleteService {

    val log = KotlinLogging.logger {}

    override fun getByTrainerId(id: Long): List<Athlete> {
        log.trace { "S | getByTrainerId($id)" }
        if (trainerRepo.findById(id).isEmpty) {
            throw ForbiddenException("You are not allowed to use this service")
        }
        return this.athleteRepository.findAllByTrainerId(id)
    }


    override fun getById(id: Long): Athlete {
        log.trace { "S | getById($id)" }
        return this.athleteRepository.findByIdOrNull(id) ?: throw NotFoundException("Could not find user by given id")
    }
}
