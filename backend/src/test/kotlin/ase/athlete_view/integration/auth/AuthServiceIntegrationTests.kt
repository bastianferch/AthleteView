package ase.athlete_view.integration.auth

import ase.athlete_view.common.exception.entity.ConflictException
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.authentication.dto.ResetPasswordDTO
import ase.athlete_view.domain.authentication.service.AuthService
import ase.athlete_view.domain.mail.service.MailService
import ase.athlete_view.domain.token.persistence.TokenRepository
import ase.athlete_view.domain.token.pojo.entity.EmailConfirmationToken
import ase.athlete_view.domain.token.pojo.entity.PasswordResetToken
import ase.athlete_view.domain.token.pojo.entity.TokenExpirationTime
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.UserCreator
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceIntegrationTests : TestBase() {
    @MockkBean
    private lateinit var mailService: MailService

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var tokenRepository: TokenRepository

    @BeforeEach
    fun setupUser() {
        super.createDefaultUserInDb(UserCreator.DEFAULT_ATHLETE_EMAIL)
        every { mailService.sendSimpleMail(any()) } just runs
    }

    @Test
    fun loginWithCorrectCredentials() {
        val login = UserCreator.getAthleteLoginDto()

        val user = this.authService.authenticateUser(login)
        assertAll(
            { assertThat(user.id).isNotZero() },
            { assertThat(user.name).isEqualTo(UserCreator.DEFAULT_ATHLETE_NAME) },
            { assertThat(user.token).isNotNull() }
        )
    }

    @Test
    fun loginWithWrongCredentials() {
        val login1 = UserCreator.getAthleteLoginDto()
        login1.email = UserCreator.DEFAULT_NON_EXISTENT_USER_EMAIL
        assertThrows<BadCredentialsException> { this.authService.authenticateUser(login1) }

        val login2 = UserCreator.getAthleteLoginDto()
        assertThat(login2.password).isNotEmpty()
        login2.password = ""
        assertThrows<BadCredentialsException> { this.authService.authenticateUser(login2) }
    }

    @Test
    fun loginWithForUnconfirmedUser() {
        val athlete = super.createDefaultUnconfirmedAthlete()
        val loginDTO = UserCreator.getAthleteLoginDto()
        loginDTO.email = athlete.email
        assertThrows<ConflictException> { this.authService.authenticateUser(loginDTO) }
    }

    @Test
    fun registerAthleteWithTrainer() {
        val existingTrainer = super.persistDefaultTrainer(100);
        val dto = UserCreator.getAthleteRegistrationDTO(existingTrainer.code)

        val athlete = this.authService.registerAthlete(dto)
        assertAll(
            { assertThat(athlete.id).isNotZero() },
            { assertThat(athlete.name).isEqualTo(UserCreator.DEFAULT_ATHLETE_NAME) },
            { assertThat(athlete.password).isNotNull() },
            { assertThat(athlete.activities).isNotNull() },
            { assertThat(athlete.preferences).isNotNull() },
            { assertThat(athlete.notifications).isNotNull() }
        )

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun registerAthleteWithoutTrainer() {
        val dto = UserCreator.getAthleteRegistrationDTO(null)

        val athlete = this.authService.registerAthlete(dto)
        assertAll(
            { assertThat(athlete.id).isNotZero() },
            { assertThat(athlete.name).isEqualTo(UserCreator.DEFAULT_ATHLETE_NAME) },
            { assertThat(athlete.password).isNotNull() },
            { assertThat(athlete.activities).isNotNull() },
            { assertThat(athlete.preferences).isNotNull() },
            { assertThat(athlete.notifications).isNotNull() }
        )

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun registerAthleteWithExistingEmailShouldFail() {
        val dto = UserCreator.getAthleteRegistrationDTO(null)
        dto.email = UserCreator.DEFAULT_ATHLETE_EMAIL
        assertThrows<ConflictException> { this.authService.registerAthlete(dto) }

        verify(exactly = 0) { mailService.sendSimpleMail(any()) }
    }


    @Test
    fun registerAthleteWithInvalidPassword() {
        val dto = UserCreator.getAthleteRegistrationDTO(null)
        dto.password = null
        assertThrows<ValidationException> { this.authService.registerAthlete(dto) }

        dto.password = "a" // short
        assertThrows<ValidationException> { this.authService.registerAthlete(dto) }

        dto.password = "aaaaaaa" // short
        assertThrows<ValidationException> { this.authService.registerAthlete(dto) }

        dto.password = "a".repeat(300) // long
        assertThrows<ValidationException> { this.authService.registerAthlete(dto) }

        dto.password = "aaaaaaaa"
        assertDoesNotThrow { this.authService.registerAthlete(dto) }

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun registerAthleteWithInvalidMail() {
        val dto = UserCreator.getAthleteRegistrationDTO(null)
        dto.email = null
        assertThrows<ValidationException> { this.authService.registerAthlete(dto) }

        dto.email = "a".repeat(300) // long
        assertThrows<ValidationException> { this.authService.registerAthlete(dto) }

        dto.email = "a@a"
        assertDoesNotThrow { this.authService.registerAthlete(dto) }

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun registerAthleteWithInvalidHeight() {
        val dto = UserCreator.getAthleteRegistrationDTO(null)
        dto.height = -1
        assertThrows<ValidationException> { this.authService.registerAthlete(dto) }

        dto.height = 0
        assertThrows<ValidationException> { this.authService.registerAthlete(dto) }

        dto.height = 30000
        assertThrows<ValidationException> { this.authService.registerAthlete(dto) }

        dto.height = 1000
        assertDoesNotThrow { this.authService.registerAthlete(dto) }

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun registerAthleteWithInvalidWeight() {
        val dto = UserCreator.getAthleteRegistrationDTO(null)
        dto.weight = -1
        assertThrows<ValidationException> { this.authService.registerAthlete(dto) }

        dto.weight = 0
        assertThrows<ValidationException> { this.authService.registerAthlete(dto) }

        dto.weight = 750000
        assertThrows<ValidationException> { this.authService.registerAthlete(dto) }

        dto.weight = 100000
        assertDoesNotThrow { this.authService.registerAthlete(dto) }

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun registerAthleteWithInvalidDoB() {
        val dto = UserCreator.getAthleteRegistrationDTO(null)
        dto.dob = null
        assertThrows<NullPointerException> { this.authService.registerAthlete(dto) }

        dto.dob = LocalDate.now()
        assertThrows<ValidationException> { this.authService.registerAthlete(dto) }

        dto.dob = UserCreator.DEFAULT_ATHLETE_DOB
        assertDoesNotThrow { this.authService.registerAthlete(dto) }

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun registerTrainer() {
        val dto = UserCreator.getTrainerRegistrationDTO()
        val trainer = this.authService.registerTrainer(dto)

        assertAll(
            { assertThat(trainer.id).isNotZero() },
            { assertThat(trainer.name).isEqualTo(UserCreator.DEFAULT_TRAINER_NAME) },
            { assertThat(trainer.password).isNotNull() },
            { assertThat(trainer.activities).isNotNull() },
            { assertThat(trainer.preferences).isNotNull() },
            { assertThat(trainer.notifications).isNotNull() }
        )
        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun confirmRegistrationWithCorrectUUID() {
        val athlete = super.createDefaultUnconfirmedAthlete()
        val token = tokenRepository.save(
            EmailConfirmationToken(
                uuid = null,
                expiryDate = TokenExpirationTime.ONE_HOUR.expirationDate(),
                user = athlete
            )
        )
        assertDoesNotThrow { token.uuid?.let { this.authService.confirmRegistration(it) } }
    }

    @Test
    fun confirmRegistrationWithWrongUUID() {
        val athlete = super.createDefaultUnconfirmedAthlete()
        val token = tokenRepository.save(
            EmailConfirmationToken(
                uuid = null,
                expiryDate = TokenExpirationTime.ONE_HOUR.expirationDate(),
                user = athlete
            )
        )

        assertThrows<NotFoundException> { this.authService.confirmRegistration(UUID.randomUUID()) }
    }

    @Test
    fun sendNewConfirmationLink() {
        val athlete = super.createDefaultUnconfirmedAthlete()
        val dto = LoginDTO(athlete.email, password = UserCreator.DEFAULT_ATHLETE_PASSWORD)

        assertDoesNotThrow { this.authService.sendNewConfirmationToken(dto) }

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun sendNewConfirmationLinkWithWrongData() {
        val athlete = super.createDefaultUnconfirmedAthlete()
        val dto = LoginDTO(
            UserCreator.DEFAULT_NEW_ATHLETE_EMAIL,
            password = UserCreator.DEFAULT_ATHLETE_PASSWORD + " wrong password"
        )

        assertThrows<BadCredentialsException> { this.authService.sendNewConfirmationToken(dto) }

        verify(exactly = 0) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun sendNewConfirmationLinkForAlreadyConfirmedUser_Returns204() {
        val dto = LoginDTO(UserCreator.DEFAULT_ATHLETE_EMAIL, password = UserCreator.DEFAULT_ATHLETE_PASSWORD)

        assertDoesNotThrow { this.authService.sendNewConfirmationToken(dto) }
        verify(exactly = 0) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun forgotPassword() {
        this.authService.forgotPassword(UserCreator.DEFAULT_ATHLETE_EMAIL)

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun forgotPasswordWithNotExistingEmail() {
        this.authService.forgotPassword(UserCreator.DEFAULT_ATHLETE_EMAIL + " wrong email")

        verify(exactly = 0) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun updateNewPassword() {
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

        if (dto != null) {
            assertDoesNotThrow { this.authService.setNewPassword(dto) }
        }
    }
}