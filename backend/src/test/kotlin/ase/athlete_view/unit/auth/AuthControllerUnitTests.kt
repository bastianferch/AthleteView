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
package ase.athlete_view.unit.auth

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.config.SecurityConfig
import ase.athlete_view.config.jwt.UserAuthProvider
import ase.athlete_view.domain.authentication.controller.AuthenticationController
import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.authentication.dto.ResetPasswordDTO
import ase.athlete_view.domain.authentication.service.AuthService
import ase.athlete_view.util.UserCreator.Companion.DEFAULT_ATHLETE_EMAIL
import ase.athlete_view.util.UserCreator.Companion.DEFAULT_ATHLETE_ID
import ase.athlete_view.util.UserCreator.Companion.DEFAULT_ATHLETE_PASSWORD
import ase.athlete_view.util.UserCreator.Companion.DEFAULT_USER_TOKEN_UUID
import ase.athlete_view.util.UserCreator.Companion.getAthlete
import ase.athlete_view.util.UserCreator.Companion.getAthleteDTO
import ase.athlete_view.util.UserCreator.Companion.getAthleteLoginDto
import ase.athlete_view.util.UserCreator.Companion.getAthleteRegistrationDTO
import ase.athlete_view.util.UserCreator.Companion.getTrainer
import ase.athlete_view.util.UserCreator.Companion.getTrainerRegistrationDTO
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.*


@WebMvcTest(controllers = [AuthenticationController::class])
@ContextConfiguration(classes = [SecurityConfig::class, AthleteViewApplication::class])
@ActiveProfiles("test")
class AuthControllerUnitTests {
    @Autowired
    private lateinit var webContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var authService: AuthService

    @MockkBean
    lateinit var authProvider: UserAuthProvider

    @Autowired
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun mvcSetup() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webContext)
            .apply<DefaultMockMvcBuilder?>(springSecurity())
            .build()
    }

    @Test
    fun givenExistingUser_LoginReturnsOk() {
        every { authService.authenticateUser(any<LoginDTO>()) } returns getAthleteDTO()

        val login = getAthleteLoginDto()
        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(login))
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(DEFAULT_ATHLETE_ID))

        verify(exactly = 1) { authService.authenticateUser(login) }
    }

    @Test
    fun givenNoUser_Login_ReturnsForbidden() {
        every { authService.authenticateUser(any<LoginDTO>()) } throws BadCredentialsException("Either email or password is incorrect")

        val login = getAthleteLoginDto()
        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(login))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun registerAthleteWithTrainer_Returns201() {
        every { authService.registerAthlete(any()) } returns getAthlete(null)

        val dto = getAthleteRegistrationDTO(null)

        mockMvc.perform(
            post("/api/auth/registration/athlete")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$").isMap)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.email").isNotEmpty)
            .andExpect(jsonPath("$.password").isEmpty)
    }

    @Test
    fun registerTrainer_Returns201() {
        every { authService.registerTrainer(any()) } returns getTrainer()

        val dto = getTrainerRegistrationDTO()

        mockMvc.perform(
            post("/api/auth/registration/trainer")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$").isMap)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.email").isNotEmpty)
            .andExpect(jsonPath("$.password").isEmpty)
    }

    @Test
    fun confirmRegistrationWithoutUUID_Returns400(){
        mockMvc.perform(
            post("/api/auth/confirmation")
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun confirmRegistrationWithWrongUUID_Returns400(){
        mockMvc.perform(
            post("/api/auth/confirmation")
                .with(csrf())
                .param("token", "not valid UUID")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun confirmRegistrationWithCorrectUUID_Returns204(){
        every { authService.confirmRegistration(any()) } just runs

        mockMvc.perform(
            post("/api/auth/confirmation")
                .with(csrf())
                .param("token", DEFAULT_USER_TOKEN_UUID)
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun sendNewConfirmationLink_Returns204(){
        every { authService.sendNewConfirmationToken(any()) } just runs
        val dto = LoginDTO(DEFAULT_ATHLETE_EMAIL, password = DEFAULT_ATHLETE_PASSWORD)

        mockMvc.perform(
            post("/api/auth/confirmation/new")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun forgotPasswordWithoutEmail_Returns400(){
        mockMvc.perform(
            post("/api/auth/forgot-password")
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun forgotPassword_Returns204(){
        every { authService.forgotPassword(any()) } just runs

        mockMvc.perform(
            post("/api/auth/forgot-password")
                .with(csrf())
                .content(objectMapper.writeValueAsString(DEFAULT_ATHLETE_EMAIL))
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun updateNewPassword_Returns204(){
        every { authService.setNewPassword(any()) } just runs

        val dto = ResetPasswordDTO(token = UUID.fromString(DEFAULT_USER_TOKEN_UUID), password = DEFAULT_ATHLETE_PASSWORD)
        mockMvc.perform(
            post("/api/auth/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
            .andExpect(status().isNoContent)
    }
}
