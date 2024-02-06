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
package ase.athlete_view.unit.user

import ase.athlete_view.common.exception.entity.ForbiddenException
import ase.athlete_view.domain.user.persistence.AthleteRepository
import ase.athlete_view.domain.user.persistence.TrainerRepository
import ase.athlete_view.domain.user.service.AthleteService
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.UserCreator
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles("test")
class AthleteServiceUnitTests: TestBase() {

    @Autowired
    private lateinit var athleteService: AthleteService

    @MockkBean
    private lateinit var trainerRepo: TrainerRepository

    @MockkBean
    private lateinit var athleteRepo: AthleteRepository

    @Test
    fun getAhletesByTrainerIdWithAthleteId_shouldThrowForbiddenException(){
        every { trainerRepo.findById(any<Long>()) } returns Optional.empty()
        assertThrows<ForbiddenException> {athleteService.getByTrainerId(1)  }
    }

    @Test
    fun getAthletesByTrainerIdWithTrainerId_shouldReturnListOfAthletes(){
        every { trainerRepo.findById(any<Long>()) } returns Optional.of(UserCreator.getTrainer())
        every { athleteRepo.findAllByTrainerId(any<Long>()) } returns listOf(UserCreator.getAthlete(1), UserCreator.getAthlete(2),UserCreator.getAthlete(3))
        val athletes = athleteService.getByTrainerId(1)

        assertAll(
            { assertEquals(athletes.size, 3) },
            { assertEquals(athletes[0].id, 1L) },
            { assertEquals(athletes[1].id, 2L) },
            { assertEquals(athletes[2].id, 3L) }
        )
    }

}
