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
