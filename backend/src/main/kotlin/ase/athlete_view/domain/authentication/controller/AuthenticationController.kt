package ase.athlete_view.domain.authentication.controller

import ase.athlete_view.config.jwt.UserAuthProvider
import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.authentication.dto.RegistrationDTO
import ase.athlete_view.domain.authentication.service.AuthenticationService
import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.domain.user.service.mapper.UserMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import lombok.AllArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@AllArgsConstructor
@RequestMapping("api/auth")
class AuthenticationController(
    private val authenticationService: AuthenticationService,
    private val userMapper: UserMapper) {
    private val logger = KotlinLogging.logger {}

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registration")
    fun registration(@RequestBody registrationDto: RegistrationDTO): UserDto {
        logger.info { "POST USER REGISTRATION $registrationDto" }

        val savedUser = this.authenticationService.registerUser(this.userMapper.toEntity(registrationDto))
        return userMapper.toDTO(savedUser)
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/login")
    fun login(@RequestBody dto: LoginDTO): UserDto {
        logger.info { "POST LOGIN FOR USER ${dto.email}" }

        return this.authenticationService.authenticateUser(dto)
    }

}
