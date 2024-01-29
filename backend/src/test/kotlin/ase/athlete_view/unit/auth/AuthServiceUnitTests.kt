package ase.athlete_view.unit.auth

import ase.athlete_view.common.exception.entity.ConflictException
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.config.jwt.UserAuthProvider
import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.authentication.dto.ResetPasswordDTO
import ase.athlete_view.domain.authentication.service.AuthService
import ase.athlete_view.domain.mail.service.MailService
import ase.athlete_view.domain.time_constraint.service.TimeConstraintService
import ase.athlete_view.domain.token.persistence.TokenRepository
import ase.athlete_view.domain.token.service.TokenService
import ase.athlete_view.domain.user.service.TrainerService
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.domain.zone.service.ZoneService
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.TokenCreator
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceUnitTests : TestBase() {
    @MockkBean
    private lateinit var mailService: MailService

    @Autowired
    private lateinit var authService: AuthService

    @MockkBean
    private lateinit var tokenRepository: TokenRepository

    @MockkBean
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var userAuthProvider: UserAuthProvider

    @MockkBean
    private lateinit var trainerService: TrainerService

    @MockkBean
    private lateinit var tokenService: TokenService

    @MockkBean
    private lateinit var timeConstraintService: TimeConstraintService

    @MockkBean
    private lateinit var zoneService: ZoneService

    private val encoder: BCryptPasswordEncoder = BCryptPasswordEncoder() // could not syncWithMockServer it.

    @BeforeEach
    fun setupUser() {
        every { mailService.sendSimpleMail(any()) } just runs
    }

    @Test
    fun loginWithCorrectCredentials() {
        val athlete = UserCreator.getAthlete(1)
        athlete.isConfirmed = true
        athlete.password = encoder.encode(athlete.password)
        every { userService.getByEmail(any()) } returns athlete
        every { userAuthProvider.createToken(any()) } returns UUID.randomUUID().toString()

        val login = UserCreator.getAthleteLoginDto()

        val user = this.authService.authenticateUser(login)
        assertAll(
            { assertThat(user.id).isNotZero() },
            { assertThat(user.name).isEqualTo(UserCreator.DEFAULT_ATHLETE_NAME) },
            { assertThat(user.token).isNotNull() },
        )
    }

    @Test
    fun loginWithWrongCredentials() {
        val athlete = UserCreator.getAthlete(1)
        athlete.password = encoder.encode(athlete.password)
        every { userService.getByEmail(UserCreator.DEFAULT_NON_EXISTENT_USER_EMAIL) } throws NotFoundException("Could not find user by given email")
        every { userService.getByEmail(athlete.email) } returns athlete
        every { userAuthProvider.createToken(any()) } returns UUID.randomUUID().toString()

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
        val athlete = UserCreator.getAthlete(1)
        athlete.password = encoder.encode(athlete.password)
        athlete.isConfirmed = false
        every { userService.getByEmail(any()) } returns athlete
        every { userAuthProvider.createToken(any()) } returns UUID.randomUUID().toString()

        val loginDTO = UserCreator.getAthleteLoginDto()
        loginDTO.email = athlete.email
        assertThrows<ConflictException> { this.authService.authenticateUser(loginDTO) }
    }

    @Test
    fun registerAthleteWithTrainer() {
        val dto = UserCreator.getAthleteRegistrationDTO(UserCreator.DEFAULT_TRAINER_CODE)
        every { dto.email?.let { userService.getByEmail(it) } } throws NotFoundException("Could not find user by given email")
        every { trainerService.getByCode(any()) } returns UserCreator.getTrainer()
        every { userService.saveAll(any()) } returns listOf()
        every { userService.save(any()) } returns UserCreator.getAthlete(1)
        every { tokenService.deleteAllRegistrationTokensByUser(any()) } just runs
        every { zoneService.resetZones(any()) } returns listOf()
        every { timeConstraintService.createDefaultTimeConstraintsForUser(any()) } just runs
        every {
            tokenService.createEmailConfirmationToken(
                any(),
                any()
            )
        } returns TokenCreator.getDefaultEmailConfirmationToken()

        val athlete = this.authService.registerAthlete(dto)

        assertAll(
            { assertThat(athlete.id).isNotZero() },
            { assertThat(athlete.name).isEqualTo(UserCreator.DEFAULT_ATHLETE_NAME) },
            { assertThat(athlete.password).isNotNull() }
        )

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun registerAthleteWithoutTrainer() {
        val dto = UserCreator.getAthleteRegistrationDTO(null)
        every { dto.email?.let { userService.getByEmail(it) } } throws NotFoundException("Could not find user by given email")
        every { userService.save(any()) } returns UserCreator.getAthlete(1)
        every { tokenService.deleteAllRegistrationTokensByUser(any()) } just runs
        every { timeConstraintService.createDefaultTimeConstraintsForUser(any()) } just runs
        every { zoneService.resetZones(any()) } returns listOf()
        every {
            tokenService.createEmailConfirmationToken(
                any(),
                any()
            )
        } returns TokenCreator.getDefaultEmailConfirmationToken()
        every { trainerService.getByCode(any()) } returns null

        val athlete = this.authService.registerAthlete(dto)
        assertAll(
            { assertThat(athlete.id).isNotZero() },
            { assertThat(athlete.name).isEqualTo(UserCreator.DEFAULT_ATHLETE_NAME) },
            { assertThat(athlete.password).isNotNull() }
        )

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun registerAthleteWithExistingEmailShouldFail() {
        val dto = UserCreator.getAthleteRegistrationDTO(null)
        every { dto.email?.let { userService.getByEmail(it) } } returns UserCreator.getAthlete(1)

        assertThrows<ConflictException> { this.authService.registerAthlete(dto) }

        verify(exactly = 0) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun registerAthleteWithInvalidPassword() {
        val dto = UserCreator.getAthleteRegistrationDTO(null)
        every { dto.email?.let { userService.getByEmail(it) } } throws NotFoundException("Could not find user by given email")
        every { userService.save(any()) } returns UserCreator.getAthlete(1)
        every { tokenService.deleteAllRegistrationTokensByUser(any()) } just runs
        every { zoneService.resetZones(any()) } returns listOf()
        every { timeConstraintService.createDefaultTimeConstraintsForUser(any()) } just runs
        every {
            tokenService.createEmailConfirmationToken(
                any(),
                any()
            )
        } returns TokenCreator.getDefaultEmailConfirmationToken()
        every { trainerService.getByCode(any()) } returns null

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
    fun registerAthleteWithInvalidHeight() {
        val dto = UserCreator.getAthleteRegistrationDTO(null)
        every { dto.email?.let { userService.getByEmail(it) } } throws NotFoundException("Could not find user by given email")
        every { userService.save(any()) } returns UserCreator.getAthlete(1)
        every { tokenService.deleteAllRegistrationTokensByUser(any()) } just runs
        every { timeConstraintService.createDefaultTimeConstraintsForUser(any()) } just runs
        every { zoneService.resetZones(any()) } returns listOf()
        every {
            tokenService.createEmailConfirmationToken(
                any(),
                any()
            )
        } returns TokenCreator.getDefaultEmailConfirmationToken()
        every { trainerService.getByCode(any()) } returns null

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
        every { dto.email?.let { userService.getByEmail(it) } } throws NotFoundException("Could not find user by given email")
        every { userService.save(any()) } returns UserCreator.getAthlete(1)
        every { tokenService.deleteAllRegistrationTokensByUser(any()) } just runs
        every { timeConstraintService.createDefaultTimeConstraintsForUser(any()) } just runs
        every { zoneService.resetZones(any()) } returns listOf()
        every {
            tokenService.createEmailConfirmationToken(
                any(),
                any()
            )
        } returns TokenCreator.getDefaultEmailConfirmationToken()
        every { trainerService.getByCode(any()) } returns null

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
        every { dto.email?.let { userService.getByEmail(it) } } throws NotFoundException("")
        every { userService.save(any()) } returns UserCreator.getAthlete(1)
        every { tokenService.deleteAllRegistrationTokensByUser(any()) } just runs
        every { timeConstraintService.createDefaultTimeConstraintsForUser(any()) } just runs
        every { zoneService.resetZones(any()) } returns listOf()

        every {
            tokenService.createEmailConfirmationToken(
                any(),
                any()
            )
        } returns TokenCreator.getDefaultEmailConfirmationToken()
        every { trainerService.getByCode(any()) } returns null

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
        every { dto.email?.let { userService.getByEmail(it) } } throws NotFoundException("Could not find user by given email")
        every { userService.save(any()) } returns UserCreator.getTrainer()
        every { tokenService.deleteAllRegistrationTokensByUser(any()) } just runs
        every { timeConstraintService.createDefaultTimeConstraintsForUser(any()) } just runs
        every {
            tokenService.createEmailConfirmationToken(
                any(),
                any()
            )
        } returns TokenCreator.getDefaultEmailConfirmationToken()
        every { trainerService.getByCode(any()) } returns null

        val trainer = this.authService.registerTrainer(dto)

        assertAll(
            { assertThat(trainer.id).isNotZero() },
            { assertThat(trainer.name).isEqualTo(UserCreator.DEFAULT_TRAINER_NAME) },
            { assertThat(trainer.password).isNotNull() }
        )
        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun confirmRegistrationWithCorrectUUID() {
        val athlete = UserCreator.getAthlete(1)
        athlete.isConfirmed = false
        every { tokenService.getUserByToken(any()) } returns athlete
        every { userService.save(any()) } returns UserCreator.getAthlete(1)
        every { tokenService.deleteToken(any()) } just runs

        assertDoesNotThrow { this.authService.confirmRegistration(TokenCreator.DEFAULT_EMAIL_CONFIRMATION_TOKEN_UUID) }
    }

    @Test
    fun confirmRegistrationWithWrongUUID() {
        val athlete = UserCreator.getAthlete(1)
        athlete.isConfirmed = false
        every { tokenService.getUserByToken(any()) } throws NotFoundException("Could not find such a token.")

        assertThrows<NotFoundException> { this.authService.confirmRegistration(UUID.randomUUID()) }
    }

    @Test
    fun sendNewConfirmationLink() {
        val athlete = UserCreator.getAthlete(1)
        athlete.isConfirmed = false
        athlete.password = encoder.encode(athlete.password)
        val dto = LoginDTO(athlete.email, password = UserCreator.DEFAULT_ATHLETE_PASSWORD)
        every { dto.email.let { userService.getByEmail(it) } } returns athlete
        every { tokenService.deleteAllRegistrationTokensByUser(any()) } just runs
        every {
            tokenService.createEmailConfirmationToken(
                any(),
                any()
            )
        } returns TokenCreator.getDefaultEmailConfirmationToken()
        every { trainerService.getByCode(any()) } returns null
        every { userAuthProvider.createToken(any()) } returns UUID.randomUUID().toString()

        assertDoesNotThrow { this.authService.sendNewConfirmationToken(dto) }

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun sendNewConfirmationLinkWithWrongData() {
        val dto = LoginDTO(
            UserCreator.DEFAULT_NEW_ATHLETE_EMAIL,
            password = UserCreator.DEFAULT_ATHLETE_PASSWORD + " wrong password"
        )
        every { dto.email.let { userService.getByEmail(it) } } throws NotFoundException("Could not find user by given email")

        assertThrows<BadCredentialsException> { this.authService.sendNewConfirmationToken(dto) }

        verify(exactly = 0) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun sendNewConfirmationLinkForAlreadyConfirmedUser_Returns204() {
        val athlete = UserCreator.getAthlete(1)
        athlete.isConfirmed = true
        athlete.password = encoder.encode(athlete.password)
        val dto = LoginDTO(athlete.email, password = UserCreator.DEFAULT_ATHLETE_PASSWORD)
        every { dto.email.let { userService.getByEmail(it) } } returns athlete
        every { userAuthProvider.createToken(any()) } returns UUID.randomUUID().toString()

        assertDoesNotThrow { this.authService.sendNewConfirmationToken(dto) }
        verify(exactly = 0) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun forgotPassword() {
        val athlete = UserCreator.getAthlete(1)

        every { userService.getByEmail(any()) } returns athlete
        every { tokenService.deleteAllPasswordResetTokensByUser(any()) } just runs
        every {
            tokenService.createResetPasswordToken(
                any(),
                any()
            )
        } returns TokenCreator.getDefaultPasswordResetToken()

        this.authService.forgotPassword(UserCreator.DEFAULT_ATHLETE_EMAIL)

        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun forgotPasswordWithNotExistingEmail() {
        every { userService.getByEmail(any()) } throws NotFoundException("Could not find user by given email")

        this.authService.forgotPassword(UserCreator.DEFAULT_ATHLETE_EMAIL + " wrong email")

        verify(exactly = 0) { mailService.sendSimpleMail(any()) }
    }

    @Test
    fun updateNewPassword() {
        val dto = ResetPasswordDTO(UserCreator.DEFAULT_TRAINER_PASSWORD, TokenCreator.DEFAULT_PASSWORD_RESET_TOKEN_UUID)
        val athlete = UserCreator.getAthlete(1)
        every { tokenService.getUserByToken(any()) } returns athlete
        every { userService.save(any()) } returns UserCreator.getAthlete(1)
        every { tokenService.deleteToken(any()) } just runs
        assertDoesNotThrow { this.authService.setNewPassword(dto) }
    }

}