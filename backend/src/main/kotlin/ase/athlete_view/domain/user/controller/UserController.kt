package ase.athlete_view.domain.user.controller

import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.domain.user.service.UserService
import lombok.AllArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@AllArgsConstructor
@RequestMapping("api/user")
class UserController (private val userService: UserService){
    // ToDo add logging.
    // private val logger = KotlinLogging.logger {}

    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    fun create(): UserDto {
        // logger.info { "POST TEST USER" }
        // ToDo add mapstruct.

        val user = userService.createTestUser()
        return UserDto(user.id, user.name)
    }
}
