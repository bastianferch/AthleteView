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
package ase.athlete_view.unit.activity

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.common.exception.entity.NotFoundException
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
import ase.athlete_view.domain.authentication.service.AuthService
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.util.ActivityCreator
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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
    lateinit var authService: AuthService

    @MockkBean
    lateinit var authProvider: UserAuthProvider

    val objectMapper = ObjectMapper().registerModules(JavaTimeModule())

    private lateinit var mockMvc: MockMvc

    private var athlete = Athlete(1, "athelte@example.com", "Athlete Doe", "athletepassword","CountryName", "12345",LocalDate.now().minusYears(20),150,70000,null, null)

    private var trainer = Trainer(1, "test@example.com", "John Doe", "secretpassword", "CountryName", "12345","", mutableSetOf(athlete), mutableSetOf())

    // Create a test object for Step class
    private val step = Step(
        null, StepType.ACTIVE, StepDurationType.DISTANCE, 30, StepDurationUnit.KM,
        StepTargetType.CADENCE, 100, 200, "Sample step note"
    )

    // Create a test object for Interval class
    val interval = Interval(null, 1, listOf(Interval(null, 2, listOf(Interval(null, 1, null, step)), null)), null)
    val plannedActivity = PlannedActivity(
        null, "test", ActivityType.RUN, interval, false, false,
        "Sample planned activity", LocalDateTime.now().plusDays(5),60,Load.MEDIUM, trainer, null, null
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
            null, "test", ActivityType.RUN, interval.toDTO(), false, false,
            "Sample planned activity", LocalDateTime.now().plusDays(5), 60, Load.MEDIUM, trainer.toUserDTO(), null,
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

    @Test
    @WithCustomMockUser(id=-1)
    fun fetchSingleActivityByIdForUser_shouldExistAndSucceed() {
        every { activityService.getSingleActivityForUser(any<Long>(), any<Long>()) } returns ActivityCreator.getDefaultActivity()

        mockMvc.perform(
                get("/api/activity/finished/1").with(csrf())
        )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

        verify (exactly = 1) { activityService.getSingleActivityForUser(-1, 1) }
    }

    @Test
    @WithCustomMockUser(id=-1)
    fun fetchSingleActivityByIdForUser_shouldNotExistAndThrow() {
        every { activityService.getSingleActivityForUser(any<Long>(), any<Long>()) } throws NotFoundException("testing")

        mockMvc.perform(
                get("/api/activity/finished/10").with(csrf())
        )
                .andExpect(status().isNotFound)

        verify (exactly = 1) { activityService.getSingleActivityForUser(-1, 10) }
    }
}
