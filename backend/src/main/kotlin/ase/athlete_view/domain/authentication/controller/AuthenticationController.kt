package ase.athlete_view.domain.authentication.controller

import ase.athlete_view.domain.authentication.dto.AthleteRegistrationDTO
import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.authentication.dto.ResetPasswordDTO
import ase.athlete_view.domain.authentication.dto.TrainerRegistrationDTO
import ase.athlete_view.domain.authentication.service.AuthenticationService
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
    private val authenticationService: AuthenticationService
) {
    private val logger = KotlinLogging.logger {}

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registration/athlete")
    fun registerAthlete(@Valid @RequestBody registrationDto: AthleteRegistrationDTO): UserDTO {
        logger.info { "POST ATHLETE REGISTRATION $registrationDto" }
        val savedUser = this.authenticationService.registerAthlete(registrationDto)
        return savedUser.toUserDTO()
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registration/trainer")
    fun registerTrainer(@Valid @RequestBody registrationDto: TrainerRegistrationDTO): UserDTO {
        logger.info { "POST TRAINER REGISTRATION ${registrationDto.email}" }
        val savedUser = this.authenticationService.registerTrainer(registrationDto)
        return savedUser.toUserDTO()
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/login")
    fun login(@RequestBody dto: LoginDTO): UserDTO {
        logger.info { "POST LOGIN FOR USER ${dto.email}" }
        return this.authenticationService.authenticateUser(dto)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/confirmation")
    fun confirmRegistration(@RequestParam token: UUID) {
        logger.info { "POST CONFIRM REGISTRATION: $token" }
        this.authenticationService.confirmRegistration(token)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/confirmation/new")
    fun sendNewConfirmationLink(@RequestBody dto: LoginDTO) {
        logger.info { "POST SEND NEW TOKEN TO EMAIL: $dto" }
        this.authenticationService.sendNewConfirmationToken(dto)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/forgot-password")
    fun sendNewConfirmationLink(@RequestBody email: String) {
        logger.info { "POST FORGOT PASSWORD FOR : $email" }
        this.authenticationService.forgotPassword(email)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/password")
    fun updateNewPassword(@RequestBody dto: ResetPasswordDTO) {
        logger.info { "POST SET NEW PASSWORD: $dto" }
        this.authenticationService.setNewPassword(dto)
    }

}
