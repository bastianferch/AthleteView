package ase.athlete_view.unit.user

import ase.athlete_view.util.TestBase
import ase.athlete_view.util.UserCreator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class UserMappingUnitTests : TestBase() {

    @Test
    @DisplayName("(+) Test athlete with trainer to dto. It is also a check, that entity was not mutated in the mapper.")
    fun mockWithoutPreviousHealth() {
        val athlete = UserCreator.getAthlete(null)
        val trainer = athlete.trainer
        trainer?.athletes?.add(athlete)

        val trainerDto = trainer?.toDto()
        assertAll(
            "Trainer check",
            { assertThat(trainerDto?.email).isEqualTo(UserCreator.getTrainer().email) },
        )

        val athleteDTO = athlete.toAthleteDto()
        assertAll(
            "Athlete check",
            { assertThat(athleteDTO.email).isEqualTo(UserCreator.getAthlete(null).email) },
            { assertThat(athleteDTO.trainer).isNotNull() },
            { assertThat(athleteDTO.trainer?.email).isEqualTo(UserCreator.getAthlete(null).trainer?.email) },
        )
    }
}
