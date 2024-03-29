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
package ase.athlete_view.unit.csp

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.config.SecurityConfig
import ase.athlete_view.config.jwt.UserAuthProvider
import ase.athlete_view.domain.authentication.controller.AuthenticationController
import ase.athlete_view.domain.authentication.service.AuthService
import ase.athlete_view.domain.csp.controller.CspController
import ase.athlete_view.domain.csp.pojo.dto.CspActivityDto
import ase.athlete_view.domain.csp.pojo.dto.CspDto
import ase.athlete_view.domain.csp.pojo.dto.CspMappingDto
import ase.athlete_view.domain.csp.pojo.entity.CspJob
import ase.athlete_view.domain.csp.service.CspService
import ase.athlete_view.domain.user.pojo.entity.Trainer
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

@WebMvcTest(controllers = [CspController::class, AuthenticationController::class])
@ContextConfiguration(classes = [SecurityConfig::class, AthleteViewApplication::class])
@ActiveProfiles("test")
class CspControllerUnitTests {
    @Autowired
    private lateinit var webContext: WebApplicationContext

    @MockkBean
    private lateinit var cspService: CspService

    @MockkBean
    lateinit var authService: AuthService

    @MockkBean
    lateinit var authProvider: UserAuthProvider

    val objectMapper = ObjectMapper().registerModules(JavaTimeModule())

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
    fun testAcceptCspReturnsAccepted() {
        val cspActivityDto: CspActivityDto = CspActivityDto(1, true)
        val cspMappingDto: CspMappingDto = CspMappingDto(1, listOf(cspActivityDto))
        val cspDto: CspDto = CspDto(listOf(cspMappingDto))
        every { cspService.accept(any<CspDto>(), any<Long>()) } returns Unit

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/csp").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(objectMapper.writeValueAsString(cspDto))
        ).andExpect(MockMvcResultMatchers.status().isAccepted())

        verify(exactly = 1) { cspService.accept(any<CspDto>(), any<Long>()) }
    }

    @Test
    @WithCustomMockUser
    fun testGetJobReturnsTrue() {
        val trainer: Trainer = Trainer(1, "test@test.com", "testerman", "test", "Austria", "4760", "ABCD", mutableSetOf(), mutableSetOf())
        val job: CspJob = CspJob(1, mutableListOf(), trainer, "2023-12-31")
        every { cspService.getJob(any<Long>()) } returns job

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/csp").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().string("true"))

        verify(exactly = 1) { cspService.getJob(any<Long>()) }
    }

    @Test
    @WithCustomMockUser
    fun testGetJobReturnsFalse() {
        every { cspService.getJob(any<Long>()) } returns null

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/csp").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().string("false"))

        verify(exactly = 1) { cspService.getJob(any<Long>()) }
    }

    @Test
    @WithCustomMockUser
    fun testRevertJobReturns() {
        every { cspService.revertJob(any<Long>()) } returns Unit
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/csp").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
        verify(exactly = 1) { cspService.revertJob(any<Long>()) }
    }
}
