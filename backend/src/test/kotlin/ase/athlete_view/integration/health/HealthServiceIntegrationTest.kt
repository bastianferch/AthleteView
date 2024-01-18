package ase.athlete_view.integration.health

import ase.athlete_view.domain.health.persistence.HealthRepository
import ase.athlete_view.domain.health.pojo.dto.HealthDTO
import ase.athlete_view.domain.health.pojo.entity.Health
import ase.athlete_view.domain.health.service.HealthService
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.util.HealthCreator
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.UserCreator
import ase.athlete_view.util.WithCustomMockUser
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import java.time.LocalDate

@ActiveProfiles("test")
@SpringBootTest
class HealthServiceIntegrationTest : TestBase() {
    @Autowired
    private lateinit var healthService: HealthService

    @Autowired
    private lateinit var healthRepository: HealthRepository

    @Autowired
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var restTemplate: RestTemplate

    @Test
    @WithCustomMockUser(id = USER_ID)
    @DisplayName("(+) mock: without previous health data")
    fun mockWithoutPreviousHealth() {
        assertThat(this.healthService.getAllByCurrentUser()).hasSize(0)
        every { restTemplate.getForObject(any<String>(), any<Class<HealthDTO>>()) } returns
                HealthDTO(
                    id = null,
                    date = HealthCreator.DEFAULT_DATE,
                    avgSteps = HealthCreator.DEFAULT_AVG_STEPS_1,
                    avgBPM = HealthCreator.DEFAULT_AVG_BPM_1,
                    avgSleepDuration = HealthCreator.DEFAULT_AVG_SLEEP_DURATION_1
                )
        this.healthService.mock()
        val persistedHealth = this.healthService.getAllByCurrentUser()
        assertThat(persistedHealth).hasSize(1)
        assertThat(persistedHealth[0].date).isEqualTo(HealthCreator.DEFAULT_DATE)
        assertThat(persistedHealth[0].user).isNotNull()
        assertThat(persistedHealth[0].user.id).isEqualTo(USER_ID)
    }

    @Test
    @WithCustomMockUser(id = USER_ID)
    @DisplayName("(+) mock: with previous health data")
    fun mockWithPreviousHealth() {
        this.healthService.save(
            Health(
                id = null,
                date = HealthCreator.DEFAULT_DATE,
                avgSteps = HealthCreator.DEFAULT_AVG_STEPS_2,
                avgBPM = HealthCreator.DEFAULT_AVG_BPM_2,
                avgSleepDuration = HealthCreator.DEFAULT_AVG_SLEEP_DURATION_2,
                user = this.userService.getById(USER_ID)
            )
        )
        assertThat(this.healthService.getAllByCurrentUser()).hasSize(1)
        every { restTemplate.getForObject(any<String>(), any<Class<HealthDTO>>()) } returns
                HealthDTO(
                    id = null,
                    date = HealthCreator.DEFAULT_DATE,
                    avgSteps = HealthCreator.DEFAULT_AVG_STEPS_1,
                    avgBPM = HealthCreator.DEFAULT_AVG_BPM_1,
                    avgSleepDuration = HealthCreator.DEFAULT_AVG_SLEEP_DURATION_1
                )
        this.healthService.mock()
        val persistedHealth = this.healthService.getAllByCurrentUser()
        assertThat(persistedHealth).hasSize(1)
        assertThat(persistedHealth[0].date).isEqualTo(HealthCreator.DEFAULT_DATE)
        assertThat(persistedHealth[0].avgSleepDuration).isEqualTo(HealthCreator.DEFAULT_AVG_SLEEP_DURATION_2)
    }

    @Test
    @DisplayName("(+) get all by current user")
    @WithCustomMockUser(id = USER_ID)
    fun getAllByCurrentUser() {
        assertThat(this.healthService.getAllByCurrentUser()).hasSize(0)
        this.healthRepository.save(
            Health(
                id = null,
                user = this.userService.getById(USER_ID),
                avgSleepDuration = 450,
                avgBPM = 70,
                avgSteps = 4000,
                date = LocalDate.of(2020, 1, 1)
            )
        )
        assertThat(this.healthService.getAllByCurrentUser()).hasSize(1)
    }

    @Test
    @WithCustomMockUser(id = USER_ID)
    fun getAllFromAthleteWithValidPreferences_ReturnsList() {
        healthRepository.save(Health(null, UserCreator.getAthlete(-2),LocalDate.now(),1,1,1))
        assertThat(this.healthService.getAllFromAthlete(-2)).hasSize(1)
    }

    @Test
    @WithCustomMockUser(id = USER_ID)
    fun getAllFromAthleteWithInValidPreferences_ReturnsEmptyList() {
        healthRepository.save(Health(null, UserCreator.getAthlete(-2),LocalDate.now(),1,1,1))
        userService.patchPreferences(UserCreator.getAthleteDTO(),UserCreator.getPreferencesDto())
        assertThat(this.healthService.getAllFromAthlete(-4)).hasSize(0)
    }

    companion object {
        private const val USER_ID = -3L
    }

}