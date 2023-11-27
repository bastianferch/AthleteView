package ase.athlete_view.integration

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.domain.activity.pojo.dto.PlannedActivityDTO
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.*
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.util.TestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDate

@SpringBootTest(
    classes = [AthleteViewApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ActivityControllerIntegrationTests: TestBase(){
    @Autowired
    private lateinit var restTemplate: TestRestTemplate


    private var trainer = Trainer(1, "test@example.com", "John Doe", "secretpassword", "CountryName", "12345")


    // Create a test object for Step class
    private val step = Step(
        null, StepType.ACTIVE, StepDurationType.DISTANCE, 30, StepDurationDistanceUnit.KM,
        StepTargetType.CADENCE, 100, 200, "Sample step note"
    )

    // Create a test object for Interval class
    val interval = Interval(null, 1, listOf(Interval(null, 2, listOf(Interval(null, 1, null, step)), null)), null)
    val plannedActivity = PlannedActivity(
        null, ActivityType.RUN, interval, false, false,
        "Sample planned activity", LocalDate.now().plusDays(5), trainer, null,
    )

    @Test
    @WithMockUser(value="asdf")
    fun createValidPlannedActivity_ShouldReturnOk(){
        val plannedActivityDto = plannedActivity.toDTO()
        val result = restTemplate.postForEntity("/api/activity/planned", plannedActivityDto, PlannedActivityDTO::class.java)
        assertThat(result).isNotNull
        assertAll(
            { assertThat(result?.statusCode).isEqualTo(HttpStatus.OK) },
            { assertThat(result?.hasBody()).isTrue() },
            { assertThat(result?.body).isNotNull() },
            { assertThat(result?.body?.id).isNotNull() },
            { assertThat(result?.body?.interval).isNotNull() },
            { assertThat(result?.body?.interval?.id).isNotNull() },
            { assertThat(result?.body?.createdFor).isEqualTo(null) },
            { assertThat(result?.body?.note).isEqualTo("Sample planned activity") },
            { assertThat(result?.body?.template).isEqualTo(false) })
    }

    @Test
    @WithMockUser(value="asdf")
    fun createInvalidPlannedActivity_ShouldReturn(){
        val plannedActivityDto = plannedActivity.toDTO()
        plannedActivityDto.interval.step = step.toDTO()
        val result = restTemplate.postForEntity("/api/activity/planned", plannedActivityDto, PlannedActivityDTO::class.java)
        assertThat(result).isNotNull
        assertAll(
            { assertThat(result?.statusCode).isEqualTo(HttpStatus.OK) },
            { assertThat(result?.hasBody()).isTrue() },
            { assertThat(result?.body).isNotNull() },
            { assertThat(result?.body?.id).isNotNull() },
            { assertThat(result?.body?.interval).isNotNull() },
            { assertThat(result?.body?.interval?.id).isNotNull() },
            { assertThat(result?.body?.createdFor).isEqualTo(null) },
            { assertThat(result?.body?.note).isEqualTo("Sample planned activity") },
            { assertThat(result?.body?.template).isEqualTo(false) })
    }
}