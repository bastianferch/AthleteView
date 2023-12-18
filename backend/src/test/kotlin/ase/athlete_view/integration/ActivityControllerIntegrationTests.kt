package ase.athlete_view.integration

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.domain.activity.persistence.PlannedActivityRepository
import ase.athlete_view.domain.activity.pojo.dto.IntervalDTO
import ase.athlete_view.domain.activity.pojo.dto.PlannedActivityDTO
import ase.athlete_view.domain.activity.pojo.dto.StepDTO
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.*
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.UserCreator
import ase.athlete_view.util.WithCustomMockUser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.io.path.absolute

@SpringBootTest(
    classes = [AthleteViewApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
//@EnableFlapdoodle
//@TestPropertySource(properties = ["app.mongodb.enabled=true"])
class ActivityControllerIntegrationTests : TestBase() {

    val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var plActRep: PlannedActivityRepository

    @Autowired
    private lateinit var userRepo: UserRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    val objectMapper = ObjectMapper().registerModules(JavaTimeModule())


    // Create a test object for Step class
    private val step = Step(
        null, StepType.ACTIVE, StepDurationType.DISTANCE, 30, StepDurationUnit.KM,
        StepTargetType.CADENCE, 100, 200, "Sample step note"
    )

    // Create a test object for Interval class
    val interval = Interval(null, 1, listOf(Interval(null, 2, listOf(Interval(null, 1, null, step)), null)), null)
    val plannedActivity = PlannedActivity(
        null, "test", ActivityType.RUN, interval, false, false,
        "Sample planned activity", LocalDateTime.now().plusDays(5), 60, Load.MEDIUM, UserCreator.getTrainer(), null, null
    )

    private lateinit var defaultAthlete: Athlete
    private lateinit var defaultTrainer: Trainer

    @BeforeEach
    fun setupUser() {
        val (athl, train) = super.createDefaultTrainerAthleteRelationInDb()
        defaultAthlete = athl
        defaultTrainer = train
    }

    @Test
    @WithCustomMockUser(id = 1)
    fun createValidPlannedActivity_ShouldReturnOk() {
        val idRegexPattern = "\"id\": null".toRegex()
        val activityTypeRegexPattern = "\"type\":\"RUN\"".toRegex()
        val plannedActivityDto = plannedActivity.toDTO()
        val result = mockMvc.perform(
            post("/api/activity/planned").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(plannedActivityDto))
        ).andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.contentAsString
        assertAll(result,
            { assert(!idRegexPattern.containsMatchIn(result)) },
            { assert(activityTypeRegexPattern.containsMatchIn(result)) }
        )
    }

    @Test
    @WithCustomMockUser(id = 2)
    fun createInvalidPlannedActivity_ShouldReturnUnprocessableEntity() {
        val validationFailedRegex = "\"message\":\"Validation of planned Activity failed".toRegex()
        val dateErrorRegex = "Date and time must be in the future".toRegex()
        val plannedActivityDto = PlannedActivityDTO(
            null, "test", ActivityType.RUN,
            IntervalDTO(
                null, 1, listOf(IntervalDTO(null, 1, null, null)), StepDTO(
                    null, StepType.ACTIVE, StepDurationType.DISTANCE, 30, StepDurationUnit.KM,
                    StepTargetType.CADENCE, 100, 200, "Sample step note"
                )
            ),
            false, false,
            "Sample planned activity", LocalDateTime.now().minusDays(5), 60, Load.MEDIUM, null, null,
        )

        val result = mockMvc.perform(
            post("/api/activity/planned").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(plannedActivityDto))
        ).andExpect(status().isUnprocessableEntity())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.contentAsString
        log.info { "Result: $result" }
        assertAll(result,
            { assert(validationFailedRegex.containsMatchIn(result)) },
            { assert(dateErrorRegex.containsMatchIn(result)) }
        )
    }

    @Test
    @WithCustomMockUser
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

    @Test
    @Transactional
    @WithCustomMockUser(id = -3)
    fun fetchAllPlannedActivities_WithoutDates_shouldReturnAllPlannedActivities() {
        // setup
//        val plannedActivity = ActivityCreator.getDefaultPlannedActivity(defaultTrainer, null, defaultAthlete)

//        mockMvc.perform(
//            post("/api/activity/planned").with(csrf())
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .characterEncoding("utf-8")
//                    .content(objectMapper.writeValueAsString(plannedActivity))
//        )
//                .andExpect(status().isOk)
//                .andDo(MockMvcResultHandlers.print())

//        val results = plActRep.findAll()
//        val users = userRepo.findAll()
//        KotlinLogging.logger {}.info { "Stored activities: $results" }
//        KotlinLogging.logger {}.info { "Stored users: $users" }

        // retrieve
        mockMvc.perform(
            get("/api/activity/planned").with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(6))
    }

    @Test
    @WithCustomMockUser(id = -3)
    fun getAllPlannedActivities_WithTimeRange_shouldReturnOnlyActivitiesInRange() {
        val starTime = OffsetDateTime.of(2023, 8, 1, 0, 0, 0, 0, ZoneOffset.of("+02:00"))
        val endTime = OffsetDateTime.of(2023, 8, 31, 0, 0, 0, 0, ZoneOffset.of("+02:00"))


        mockMvc.perform(
            get("/api/activity/planned").with(csrf())
                .param("startTime", starTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .param("endTime", endTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(1))
    }

    @Test
    @WithCustomMockUser(id = -1)
    fun getAllPlannedActivitiesForAthlete_shouldOnlyReturnOwnActivities() {
        mockMvc.perform(
            get("/api/activity/planned")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(1))
    }

    @Test
    @WithCustomMockUser(id = -4)
    fun sendFitFileForPlannedTrainingAndSearchForTrainingInItsTimePeriod_shouldReturnTrainingWithAccuracy() {
        val filePath = Paths.get("src/test/resources/fit-files/7x(1km P2').fit").absolute()
        val name = "test.fit"
        val byteContent = Files.readAllBytes(filePath)
        val resultFile = MockMultipartFile("files", name, MediaType.MULTIPART_FORM_DATA_VALUE, byteContent)

        val starTime = OffsetDateTime.of(2022, 9, 1, 0, 0, 0, 0, ZoneOffset.of("+02:00"))
        val endTime = OffsetDateTime.of(2024, 9, 30, 0, 0, 0, 0, ZoneOffset.of("+02:00"))


        mockMvc.perform(
            multipart(HttpMethod.POST, "/api/activity/import")
                .file(resultFile)
                .with(csrf())
        )
            .andExpect(status().isCreated)

        mockMvc.perform(
            get("/api/activity/finished").with(csrf())
                .param("startTime", starTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .param("endTime", endTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].accuracy", greaterThan(25)))
            .andExpect(jsonPath("$[0].activityType").value("RUN"))
    }

    @Test
    @WithCustomMockUser(id = -1)
    fun fetchSingleActivityForUserWithActivity_shouldReturnActivityAndOk() {
        mockMvc.perform(
                get("/api/activity/finished/-1").with(csrf())
        )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty)
                .andExpect(jsonPath("$.id").value(-1))
    }

    @Test
    @WithCustomMockUser(id = -1)
    fun fetchSingleActivityForUserWithInvalidActivityId_shouldReturnNotFound() {
        mockMvc.perform(
                get("/api/activity/finished/-999").with(csrf())
        )
                .andExpect(status().isNotFound)
    }

    @Test
    @WithCustomMockUser(id = -3)
    fun fetchSingleActivityForUserWithoutActivity_shouldReturnNotFound() {
        mockMvc.perform(
                get("/api/activity/finished/-1").with(csrf())
        )
                .andExpect(status().isNotFound)
    }
}
