package ase.athlete_view.domain.user.controller

import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.user.service.UserService
import lombok.AllArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@AllArgsConstructor
@RequestMapping("api/user")
class UserController (private val userService: UserService){

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/test-authorized")
    fun example(): String {
        return "You did it!"
    }


}
