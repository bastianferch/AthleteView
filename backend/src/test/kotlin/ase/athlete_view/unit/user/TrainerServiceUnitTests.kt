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
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.mail.service.MailService
import ase.athlete_view.domain.user.persistence.TrainerRepository
import ase.athlete_view.domain.user.service.TrainerService
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.UserCreator
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import io.mockk.verifyAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles("test")
class TrainerServiceUnitTests: TestBase() {

    @Autowired
    private lateinit var trainerService: TrainerService

    @MockkBean
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var trainerMockRepo: TrainerRepository

    @MockkBean
    private lateinit var mailService: MailService

    @Test
    fun resetCodeWithTrainer_ShouldNotThrow(){
        every { trainerMockRepo.getTrainerByCode(any()) } returns null
        every { trainerMockRepo.save(any()) } returns UserCreator.getTrainer()
        every { trainerMockRepo.findById(any()) } returns Optional.of(UserCreator.getTrainer())
        trainerService.resetCode(UserCreator.getTrainerDto())
        verify(exactly = 1) { trainerMockRepo.getTrainerByCode(any()) }
        verify(exactly = 1) { trainerMockRepo.save(any()) }
    }

    @Test
    fun resetCodeWithAthleteShouldThrowForbiddenException(){
        every { trainerMockRepo.getTrainerByCode(any()) } returns null
        every { trainerMockRepo.save(any()) } returns UserCreator.getTrainer()
        every { trainerMockRepo.findById(any()) } returns Optional.empty()
        assertThrows<ForbiddenException> { trainerService.resetCode(UserCreator.getTrainerDto()) }
    }

    @Test
    fun inviteAthletesWithInvalidEmail_ShouldThrowValidationException(){
        every { trainerMockRepo.findById(any()) } returns Optional.of(UserCreator.getTrainer())
        assertThrows<ValidationException> { trainerService.inviteAthletes(1, listOf("invalidEmail")) }
    }

    @Test
    fun inviteAthletesWithMultipleValidAndSingleInvalidEmail_ShouldThrowValidationException(){
        every { trainerMockRepo.findById(any()) } returns Optional.of(UserCreator.getTrainer())
        every { mailService.sendSimpleMail(any()) } returns Unit
        assertThrows<ValidationException> { trainerService.inviteAthletes(1, listOf("test@gmail.com", "invalidEmail")) }
    }

    @Test
    fun acceptAthleteAsTrainer_ShouldNotThrow(){
        val trainer = UserCreator.getTrainer()
        val athlete = UserCreator.getAthlete(1)
        athlete.trainer = null
        trainer.unacceptedAthletes += athlete

        every { trainerMockRepo.findByIdOrNull(any()) } returns trainer
        every { userService.saveAll(any()) } returnsArgument 0

        trainerService.acceptAthlete(UserCreator.getTrainerDto(), 1)

        verifyAll {
            trainerMockRepo.findByIdOrNull(any())
            userService.saveAll(listOf(trainer, athlete))
        }

        assertAll(
            { assertEquals(trainer.unacceptedAthletes.size, 0) },
            { assertEquals(trainer.athletes.size, 1) },
            { assertEquals(athlete.trainer, trainer) }
        )
    }

    @Test
    fun acceptAthleteAsTrainerWithAthleteAlreadyAccepted_ShouldThrowForbidden() {
        val trainer = UserCreator.getTrainer()
        trainer.athletes += UserCreator.getAthlete(1)

        every { trainerMockRepo.findByIdOrNull(any()) } returns trainer

        assertThrows<ForbiddenException> { trainerService.acceptAthlete(UserCreator.getTrainerDto(), 1) }
    }

    @Test
    fun acceptAthleteAsTrainerWithWrongAthlete_ShouldThrowNotFound() {
        val trainer = UserCreator.getTrainer()
        trainer.unacceptedAthletes += UserCreator.getAthlete(1)

        every { trainerMockRepo.findByIdOrNull(any()) } returns trainer

        assertThrows<NotFoundException> { trainerService.acceptAthlete(UserCreator.getTrainerDto(), 1000) }
    }
}
