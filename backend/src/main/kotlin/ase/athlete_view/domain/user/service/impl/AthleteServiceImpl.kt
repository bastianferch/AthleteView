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
