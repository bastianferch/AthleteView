package ase.athlete_view.unit.user

import ase.athlete_view.domain.user.persistence.AthleteRepository
import ase.athlete_view.util.TestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
class AthleteRepoUnitTests: TestBase() {
    @Autowired
    private lateinit var athleteRepo: AthleteRepository

    @Test
    fun findAllByTrainerId_shouldReturnListOfAthletes(){
        val athletes = athleteRepo.findAllByTrainerId(-3)
        assertEquals(athletes.size,1)
    }
}
