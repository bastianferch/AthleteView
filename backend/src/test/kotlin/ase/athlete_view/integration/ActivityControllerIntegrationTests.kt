package ase.athlete_view.integration

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.domain.activity.pojo.dto.IntervalDTO
import ase.athlete_view.domain.activity.pojo.dto.PlannedActivityDTO
import ase.athlete_view.domain.activity.pojo.dto.StepDTO
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.*
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.UserCreator
import ase.athlete_view.util.WithCustomMockUser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import kotlin.io.path.absolute

@SpringBootTest(
    classes = [AthleteViewApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ActivityControllerIntegrationTests : TestBase() {

    val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var mockMvc: MockMvc

    val objectMapper = ObjectMapper().registerModules(JavaTimeModule())


    // Create a test object for Step class
    private val step = Step(
        null, StepType.ACTIVE, StepDurationType.DISTANCE, 30, StepDurationDistanceUnit.KM,
        StepTargetType.CADENCE, 100, 200, "Sample step note"
    )

    // Create a test object for Interval class
    val interval = Interval(null, 1, listOf(Interval(null, 2, listOf(Interval(null, 1, null, step)), null)), null)
    val plannedActivity = PlannedActivity(
        null, ActivityType.RUN, interval, false, false,
        "Sample planned activity", LocalDateTime.now().plusDays(5), UserCreator.getTrainer(), null,
    )

    @BeforeEach
    fun setupUser() {
        super.createDefaultTrainerAthleteRelationInDb()
    }

    @Test
    @WithCustomMockUser(id=2)
    fun createValidPlannedActivity_ShouldReturnOk(){
        val idRegexPattern = "\"id\": null".toRegex()
        val activityTypeRegexPattern = "\"type\":\"RUN\"".toRegex()
        val plannedActivityDto = plannedActivity.toDTO()
        val result= mockMvc.perform(
            post("/api/activity/planned").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(plannedActivityDto))
        ).andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.contentAsString
        assertAll(result,
            {assert(!idRegexPattern.containsMatchIn(result))},
            {assert(activityTypeRegexPattern.containsMatchIn(result))}
        )
    }

    @Test
    @WithCustomMockUser(id=2)
    fun createInvalidPlannedActivity_ShouldReturnUnprocessableEntity() {
        val validationFailedRegex = "\"message\":\"Validation of planned Activity failed".toRegex()
        val dateErrorRegex = "Date and time must be in the future".toRegex()
        val plannedActivityDto = PlannedActivityDTO(
            null, ActivityType.RUN, IntervalDTO(null, 1, listOf(IntervalDTO(null,1,null,null)), StepDTO(
                null, StepType.ACTIVE, StepDurationType.DISTANCE, 30, StepDurationDistanceUnit.KM,
                StepTargetType.CADENCE, 100, 200, "Sample step note")), false, false,
            "Sample planned activity", LocalDateTime.now().minusDays(5), null, null,
        )

        val result = mockMvc.perform(
            post("/api/activity/planned").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(plannedActivityDto))
        ).andExpect(status().isUnprocessableEntity())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.contentAsString
        log.info{"Result: $result"}
        assertAll(result,
            {assert(validationFailedRegex.containsMatchIn(result))},
            {assert(dateErrorRegex.containsMatchIn(result))}
        )

    }

    @Test
    @WithMockUser(value = "Testuser")
    fun verifyUploadOfValidFitFile_Returns201() {
        val filePath = Paths.get("src/test/resources/fit-files/valid_file.fit").absolute()
        val name = "test.fit"
        val byteContent = Files.readAllBytes(filePath)
        val resultFile = MockMultipartFile("files", name, MediaType.MULTIPART_FORM_DATA_VALUE, byteContent)

        mockMvc.perform(
            multipart(HttpMethod.POST, "/api/activity/import")
                .file(resultFile)
                .with(csrf())
        )
            .andExpect(status().isCreated)
    }
}
