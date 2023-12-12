package ase.athlete_view.unit.activity

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.config.SecurityConfig
import ase.athlete_view.config.jwt.UserAuthProvider
import ase.athlete_view.domain.activity.controller.ActivityController
import ase.athlete_view.domain.activity.pojo.dto.PlannedActivityDTO
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.*
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.authentication.controller.AuthenticationController
import ase.athlete_view.domain.authentication.service.AuthenticationService
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.service.mapper.UserMapper
import ase.athlete_view.util.WithCustomMockUser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime


@WebMvcTest(controllers = [ActivityController::class, AuthenticationController::class])
@ContextConfiguration(classes = [SecurityConfig::class, AthleteViewApplication::class])
@ActiveProfiles("test")
class ActivityControllerUnitTests {

    @Autowired
    private lateinit var webContext: WebApplicationContext

    @MockkBean
    private lateinit var activityService: ActivityService

    @MockkBean
    lateinit var authService: AuthenticationService

    @MockkBean
    lateinit var authProvider: UserAuthProvider

    @MockkBean
    private lateinit var userMapper: UserMapper

    val objectMapper = ObjectMapper().registerModules(JavaTimeModule())

    private lateinit var mockMvc: MockMvc

    private var athlete = Athlete(1, "athelte@example.com", "Athlete Doe", "athletepassword","CountryName", "12345",LocalDate.now().minusYears(20),150,70000,null)

    private var trainer = Trainer(1, "test@example.com", "John Doe", "secretpassword", "CountryName", "12345","", mutableSetOf(athlete))

    // Create a test object for Step class
    private val step = Step(
        null, StepType.ACTIVE, StepDurationType.DISTANCE, 30, StepDurationDistanceUnit.KM,
        StepTargetType.CADENCE, 100, 200, "Sample step note"
    )

    // Create a test object for Interval class
    val interval = Interval(null, 1, listOf(Interval(null, 2, listOf(Interval(null, 1, null, step)), null)), null)
    val plannedActivity = PlannedActivity(
        null, ActivityType.RUN, interval, false, false,
        "Sample planned activity", LocalDateTime.now().plusDays(5), trainer, null,
    )

    @BeforeEach
    fun mvcSetup() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webContext)
            .apply<DefaultMockMvcBuilder?>(springSecurity())
            .build()
    }

    @Test
    @WithCustomMockUser
    fun createActivityPlanned_ReturnsOk() {
        val plannedActivityDTO = PlannedActivityDTO(
            null, ActivityType.RUN, interval.toDTO(), false, false,
            "Sample planned activity", LocalDateTime.now().plusDays(5), trainer.toUserDto(), null,
        )
        every { activityService.createPlannedActivity(any<PlannedActivity>(),any()) } returns plannedActivity

         mockMvc.perform(
            post("/api/activity/planned").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(plannedActivityDTO))
        ).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))

        verify(exactly = 1) { activityService.createPlannedActivity(any<PlannedActivity>(),any()) }
    }
}
