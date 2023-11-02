package ase.athlete_view.domain.authentication.controller

import ase.athlete_view.config.jwt.UserAuthProvider
import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.authentication.dto.RegistrationDTO
import ase.athlete_view.domain.authentication.service.AuthenticationService
import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.domain.user.pojo.entity.User
import lombok.AllArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@AllArgsConstructor
@RequestMapping("api/auth")
class AuthenticationController(private val authenticationService: AuthenticationService,
    private val userAuthProvider: UserAuthProvider) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registration")
    //ToDo return dto.
    fun registration(@RequestBody dto: RegistrationDTO): User {
        // logger.info { "POST TEST USER" }
        // ToDo add mapstruct.
        val user = User(0, dto.email, dto.name, dto.password)
        val savedUser = this.authenticationService.registerUser(user)
        user.id = savedUser.id;
        return user;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/login")
    fun login(@RequestBody user: LoginDTO): UserDto {
        println("login for " + user.email)
        val savedUser = this.authenticationService.authenticateUser(user)

        return UserDto(0, savedUser.email, savedUser.name, savedUser.password, this.userAuthProvider.createToken(savedUser))
    }

}
