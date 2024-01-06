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
    private val logger = KotlinLogging.logger {}

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registration/athlete")
    fun registerAthlete(@Valid @RequestBody registrationDto: AthleteRegistrationDTO): UserDTO {
        logger.info { "POST ATHLETE REGISTRATION $registrationDto" }
        val savedUser = this.authService.registerAthlete(registrationDto)
        return savedUser.toUserDTO()
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registration/trainer")
    fun registerTrainer(@Valid @RequestBody registrationDto: TrainerRegistrationDTO): UserDTO {
        logger.info { "POST TRAINER REGISTRATION ${registrationDto.email}" }
        val savedUser = this.authService.registerTrainer(registrationDto)
        return savedUser.toUserDTO()
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/login")
    fun login(@RequestBody dto: LoginDTO): UserDTO {
        logger.info { "POST LOGIN FOR USER ${dto.email}" }
        return this.authService.authenticateUser(dto)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/confirmation")
    fun confirmRegistration(@RequestParam token: UUID) {
        logger.info { "POST CONFIRM REGISTRATION: $token" }
        this.authService.confirmRegistration(token)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/confirmation/new")
    fun sendNewConfirmationLink(@RequestBody dto: LoginDTO) {
        logger.info { "POST SEND NEW TOKEN TO EMAIL: $dto" }
        this.authService.sendNewConfirmationToken(dto)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody email: String) {
        logger.info { "POST FORGOT PASSWORD FOR : $email" }
        this.authService.forgotPassword(email)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/password")
    fun updateNewPassword(@RequestBody dto: ResetPasswordDTO) {
        logger.info { "POST SET NEW PASSWORD: $dto" }
        this.authService.setNewPassword(dto)
    }

}
