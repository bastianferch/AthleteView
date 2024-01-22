package ase.athlete_view.domain.authentication.controller

import ase.athlete_view.domain.authentication.dto.AthleteRegistrationDTO
import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.authentication.dto.ResetPasswordDTO
import ase.athlete_view.domain.authentication.dto.TrainerRegistrationDTO
import ase.athlete_view.domain.authentication.service.AuthService
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import lombok.AllArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@AllArgsConstructor
@RequestMapping("api/auth")
class AuthenticationController(
    private val authService: AuthService
) {
    private val log = KotlinLogging.logger {}

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registration/athlete")
    fun registerAthlete(@Valid @RequestBody registrationDto: AthleteRegistrationDTO): UserDTO {
        log.info { "POST | registerAthlete($registrationDto)" }
        val savedUser = this.authService.registerAthlete(registrationDto)
        return savedUser.toUserDTO()
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registration/trainer")
    fun registerTrainer(@Valid @RequestBody registrationDto: TrainerRegistrationDTO): UserDTO {
        log.info { "POST | registerTrainer($registrationDto)" }
        val savedUser = this.authService.registerTrainer(registrationDto)
        return savedUser.toUserDTO()
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/login")
    fun login(@RequestBody dto: LoginDTO): UserDTO {
        log.info { "POST | login($dto)" }
        return this.authService.authenticateUser(dto)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/confirmation")
    fun confirmRegistration(@RequestParam token: UUID) {
        log.info { "POST | confirmRegistration($token)" }
        this.authService.confirmRegistration(token)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/confirmation/new")
    fun sendNewConfirmationLink(@RequestBody dto: LoginDTO) {
        log.info { "POST | sendNewConfirmationLink($dto)" }
        this.authService.sendNewConfirmationToken(dto)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody email: String) {
        log.info { "POST | forgotPassword($email)" }
        this.authService.forgotPassword(email)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/password")
    fun updateNewPassword(@RequestBody dto: ResetPasswordDTO) {
        log.info { "POST | updateNewPassword($dto)" }
        this.authService.setNewPassword(dto)
    }

}
