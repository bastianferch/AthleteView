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
package ase.athlete_view.integration.health

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.common.exception.entity.InternalException
import ase.athlete_view.config.SecurityConfig
import ase.athlete_view.config.jwt.UserAuthProvider
import ase.athlete_view.domain.authentication.controller.AuthenticationController
import ase.athlete_view.domain.authentication.service.AuthService
import ase.athlete_view.domain.health.controller.HealthController
import ase.athlete_view.domain.health.pojo.entity.Health
import ase.athlete_view.domain.health.service.HealthService
import ase.athlete_view.util.UserCreator
import ase.athlete_view.util.WithCustomMockUser
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDate

@ActiveProfiles("test")
@WebMvcTest(controllers = [HealthController::class, AuthenticationController::class])
@ContextConfiguration(classes = [SecurityConfig::class, AthleteViewApplication::class])
class HealthControllerIntegrationTest {
    @Autowired
    private lateinit var webContext: WebApplicationContext

    @MockkBean
    lateinit var authService: AuthService

    @MockkBean
    lateinit var healthService: HealthService

    @MockkBean
    lateinit var authProvider: UserAuthProvider

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun mvcSetup() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webContext)
            .apply<DefaultMockMvcBuilder?>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @Test
    @WithCustomMockUser
    fun syncHealthReturnsOk(){
        every { healthService.syncWithMockServer(any()) } just runs
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/health/sync").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
        ).andExpect(MockMvcResultMatchers.status().isCreated())
        verify(exactly = 1) { healthService.syncWithMockServer(any()) }
    }

    @Test
    @WithCustomMockUser
    fun syncHealthThrowsAnException(){
        every { healthService.syncWithMockServer(any()) } throws InternalException("no api today.")
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/health/sync").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
        ).andExpect(MockMvcResultMatchers.status().is5xxServerError())
        verify(exactly = 1) { healthService.syncWithMockServer(any()) }
    }

    @Test
    @WithCustomMockUser
    fun getHealth_returnsHealthDTO(){
        every { healthService.getAllByCurrentUser(any()) } returns listOf()
        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/health/stats").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsString
        verify(exactly = 1) { healthService.getAllByCurrentUser(any()) }
        assertTrue(result.contains("-1"))
    }

    @Test
    @WithCustomMockUser
    fun getHealthWithData_returnsHealthDTO(){
        every { healthService.getAllByCurrentUser(any()) } returns listOf(
            Health(null, UserCreator.getAthlete(1),LocalDate.now(),2,2,2),
            Health(null, UserCreator.getAthlete(1),LocalDate.now().minusDays(1),4,4,4))
        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/health/stats").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsString
        verify(exactly = 1) { healthService.getAllByCurrentUser(any()) }
        assertAll(
            { assertTrue(result.contains(":2,")) },
            { assertTrue(!result.contains(":4,")) }
        )

    }

    @Test
    @WithCustomMockUser(-3)
    fun getHealthFromAthlete_returnsHealthDTO(){
        every { healthService.getAllFromAthlete(-2, any()) } returns listOf(
            Health(null, UserCreator.getAthlete(-2),LocalDate.now(),2,2,2),
            Health(null, UserCreator.getAthlete(-2),LocalDate.now().minusDays(1),4,4,4))
        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/health/stats/-2").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsString
        verify(exactly = 1) { healthService.getAllFromAthlete(-2, any()) }
        assertAll(
            { assertTrue(result.contains(":2,")) },
            { assertTrue(!result.contains(":4,")) }
        )

    }
}
