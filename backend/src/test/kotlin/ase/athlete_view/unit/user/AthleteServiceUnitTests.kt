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
