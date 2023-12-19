package ase.athlete_view.domain.user.service.impl

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.user.persistence.AthleteRepository
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.service.AthleteService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class AthleteServiceImpl(val athleteRepository: AthleteRepository) : AthleteService {

    val log = KotlinLogging.logger {}

    override fun getByTrainerId(id: Long): List<Athlete> {
        log.trace { "getByTrainerId $id" }
        return this.athleteRepository.findAllByTrainerId(id)
    }


    override fun getById(id: Long): Athlete {
        log.trace { "getById $id" }
        return this.athleteRepository.findByIdOrNull(id) ?: throw NotFoundException("Could not find user by given id")
    }
}
