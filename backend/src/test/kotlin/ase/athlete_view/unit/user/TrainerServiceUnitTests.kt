package ase.athlete_view.unit.user

import ase.athlete_view.common.exception.entity.ForbiddenException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.mail.service.MailService
import ase.athlete_view.domain.user.persistence.TrainerRepository
import ase.athlete_view.domain.user.service.TrainerService
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.UserCreator
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles("test")
class TrainerServiceUnitTests: TestBase() {

    @Autowired
    private lateinit var trainerService: TrainerService

    @Autowired
    private lateinit var trainerRepo: TrainerRepository

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
        // use TrainerRepo instead of TrainerMockRepo
    }
}