package ase.athlete_view.integration.auth

import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.authentication.dto.ResetPasswordDTO
import ase.athlete_view.domain.mail.service.MailService
import ase.athlete_view.domain.token.persistence.TokenRepository
import ase.athlete_view.domain.token.pojo.entity.EmailConfirmationToken
import ase.athlete_view.domain.token.pojo.entity.PasswordResetToken
import ase.athlete_view.domain.token.pojo.entity.TokenExpirationTime
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.UserCreator
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTests : TestBase() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var mailService: MailService

    @Autowired
    private lateinit var tokenRepository: TokenRepository
    @BeforeEach
    fun setupUser() {
        super.createDefaultUserInDb(UserCreator.DEFAULT_ATHLETE_EMAIL)
    }

    @Test
    fun loginWithCorrectCredentials_ShouldReturnOk() {
        val login = UserCreator.getAthleteLoginDto()

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(login))
        )
            .andExpect(status().isOk())
    }

    @Test
    fun givenNoUser_Login_ReturnsForbidden() {
        val login = UserCreator.getNonExistentUserLoginDto()
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
        every { mailService.sendSimpleMail(any()) } just runs

        val existingTrainer = super.persistDefaultTrainer(100)
        val dto = UserCreator.getAthleteRegistrationDTO(existingTrainer.code)

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

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun registerAthleteWithoutTrainer_Returns201() {
        every { mailService.sendSimpleMail(any()) } just runs

        val dto = UserCreator.getAthleteRegistrationDTO(null)

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

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun registerTrainer_Returns201() {
        every { mailService.sendSimpleMail(any()) } just runs

        val dto = UserCreator.getTrainerRegistrationDTO()

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

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }


    @Test
    fun confirmRegistrationWithCorrectUUID_Returns204() {
        val athlete = super.createDefaultUnconfirmedAthlete()
        val token = tokenRepository.save(
            EmailConfirmationToken(
                uuid = null,
                expiryDate = TokenExpirationTime.ONE_HOUR.expirationDate(),
                user = athlete
            )
        )

        mockMvc.perform(
            post("/api/auth/confirmation")
                .with(csrf())
                .param("token", token.uuid.toString())
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun sendNewConfirmationLink_Returns204() {
        every { mailService.sendSimpleMail(any()) } just runs
        val athlete = super.createDefaultUnconfirmedAthlete()
        val dto = LoginDTO(athlete.email, password = UserCreator.DEFAULT_ATHLETE_PASSWORD)

        mockMvc.perform(
            post("/api/auth/confirmation/new")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
            .andExpect(status().isNoContent)

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun sendNewConfirmationLinkForAlreadyConfirmedUser_Returns204() {
        every { mailService.sendSimpleMail(any()) } just runs

        val dto = LoginDTO(UserCreator.DEFAULT_ATHLETE_EMAIL, password = UserCreator.DEFAULT_ATHLETE_PASSWORD)

        mockMvc.perform(
            post("/api/auth/confirmation/new")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
            .andExpect(status().isNoContent)

        verify(exactly = 0) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun forgotPassword_Returns204() {
        every { mailService.sendSimpleMail(any()) } just runs

        mockMvc.perform(
            post("/api/auth/forgot-password")
                .with(csrf())
                .content(UserCreator.DEFAULT_ATHLETE_EMAIL)
        )
            .andExpect(status().isNoContent)

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun updateNewPassword_Returns204() {
        val user = super.createDefaultUserInDb(UserCreator.DEFAULT_NEW_ATHLETE_EMAIL)
        val token = tokenRepository.save(
            PasswordResetToken(
                uuid = null,
                expiryDate = TokenExpirationTime.ONE_HOUR.expirationDate(),
                user = user
            )
        )
        val dto = token.uuid?.let {
            ResetPasswordDTO(
                token = it,
                password = UserCreator.DEFAULT_ATHLETE_PASSWORD
            )
        }
        mockMvc.perform(
            post("/api/auth/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
            .andExpect(status().isNoContent)
    }
}