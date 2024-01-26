package ase.athlete_view.integration.health
import ase.athlete_view.common.exception.entity.InternalException
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
import org.apache.http.conn.HttpHostConnectException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    @DisplayName("(+) syncWithMockServer: without previous health data")
    fun mockWithoutPreviousHealth() {
        assertThat(this.healthService.getAllByCurrentUser(USER_ID)).hasSize(0)
        every { restTemplate.getForObject(any<String>(), any<Class<Array<HealthDTO>>>()) } returns
                HealthCreator.getDefaultHealthForOneWeekDto1()
        this.healthService.syncWithMockServer(USER_ID)
        val persistedHealth = this.healthService.getAllByCurrentUser(USER_ID)
        assertThat(persistedHealth).hasSize(7)
        assertThat(persistedHealth[0].date).isEqualTo(HealthCreator.DEFAULT_DATE_1)
        assertThat(persistedHealth[0].user).isNotNull()
        assertThat(persistedHealth[0].user.id).isEqualTo(USER_ID)
    }

    @Test
    @WithCustomMockUser(id = USER_ID)
    @DisplayName("(+) syncWithMockServer: with some previous health data")
    fun mockWithSomePreviousHealth() {
        val user = this.userService.getById(USER_ID)
        this.healthRepository.saveAll(HealthCreator.defaultHealthForOneWeek_1(user).subList(0,3))
        val healthBeforeMock = this.healthService.getAllByCurrentUser(USER_ID)
        assertThat(healthBeforeMock).hasSize(3)
        every { restTemplate.getForObject(any<String>(), any<Class<Array<HealthDTO>>>()) } returns
                HealthCreator.defaultHealthForOneWeekDto2()
        this.healthService.syncWithMockServer(USER_ID)
        val persistedHealth = this.healthService.getAllByCurrentUser(USER_ID)
        assertThat(persistedHealth).hasSize(7)
        assertThat(persistedHealth[0].date).isEqualTo(HealthCreator.DEFAULT_DATE_1)
        assertThat(persistedHealth[0].avgSleepDuration).isEqualTo(HealthCreator.DEFAULT_AVG_SLEEP_DURATION_1)
        assertThat(persistedHealth[0].avgSteps).isEqualTo(HealthCreator.DEFAULT_AVG_STEPS_1)
    }

    @Test
    @WithCustomMockUser(id = USER_ID)
    @DisplayName("(-) syncWithMockServer: previous data exists and is not overridden")
    fun mockWithPreviousHealth() {
        val user = this.userService.getById(USER_ID)
        this.healthRepository.saveAll(HealthCreator.defaultHealthForOneWeek_1(user))
        val healthBeforeMock = this.healthService.getAllByCurrentUser(USER_ID)
        assertThat(healthBeforeMock).hasSize(7)
        every { restTemplate.getForObject(any<String>(), any<Class<Array<HealthDTO>>>()) } returns
                HealthCreator.defaultHealthForOneWeekDto2()
        this.healthService.syncWithMockServer(USER_ID)
        val persistedHealth = this.healthService.getAllByCurrentUser(USER_ID)
        assertThat(persistedHealth).hasSize(7)
        assertThat(persistedHealth[0].date).isEqualTo(HealthCreator.DEFAULT_DATE_1)
        assertThat(persistedHealth[0].avgSleepDuration).isEqualTo(HealthCreator.DEFAULT_AVG_SLEEP_DURATION_1)
        assertThat(persistedHealth[0].avgSteps).isEqualTo(HealthCreator.DEFAULT_AVG_STEPS_1)
    }

    @Test
    @WithCustomMockUser(id = USER_ID)
    @DisplayName("(-) syncWithMockServer: no connection to db.")
    fun mockNoConnectionToPrevDB() {
        every { restTemplate.getForObject(any<String>(), any<Class<Array<HealthDTO>>>()) } throws HttpHostConnectException(null,null,null)
        assertThrows<InternalException> { this.healthService.syncWithMockServer(USER_ID)}
    }

    @Test
    @DisplayName("(+) get all by current user")
    @WithCustomMockUser(id = USER_ID)
    fun getAllByCurrentUser() {
        assertThat(this.healthService.getAllByCurrentUser(USER_ID)).hasSize(0)
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
        assertThat(this.healthService.getAllByCurrentUser(USER_ID)).hasSize(1)
    }

    @Test
    @WithCustomMockUser(id = USER_ID)
    fun getAllFromAthleteWithValidPreferences_ReturnsList() {
        healthRepository.save(Health(null, UserCreator.getAthlete(-2), LocalDate.now(), 1, 1, 1))
        assertThat(this.healthService.getAllFromAthlete(-2, USER_ID)).hasSize(1)
    }

    @Test
    @WithCustomMockUser(id = USER_ID)
    fun getAllFromAthleteWithInValidPreferences_ReturnsEmptyList() {
        healthRepository.save(Health(null, UserCreator.getAthlete(-2), LocalDate.now(), 1, 1, 1))
        userService.patchPreferences(UserCreator.getAthleteDTO(), UserCreator.getPreferencesDto())
        assertThat(this.healthService.getAllFromAthlete(-4, USER_ID)).hasSize(0)
    }

    companion object {
        private const val USER_ID = -3L
    }

}