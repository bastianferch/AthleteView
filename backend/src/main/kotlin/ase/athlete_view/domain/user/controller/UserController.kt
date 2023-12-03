package ase.athlete_view.domain.user.controller

import ase.athlete_view.common.exception.entity.ForbiddenException
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import ase.athlete_view.domain.user.pojo.dto.TrainerDTO
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.domain.user.service.mapper.UserMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/user")
class UserController (private val userService: UserService,
    private val userMapper: UserMapper){
    val log = KotlinLogging.logger {}

    @ResponseStatus(HttpStatus.OK)
    @GetMapping()
    fun get(@AuthenticationPrincipal userDTO: UserDTO): UserDTO {
        log.info { "GET USER ${userDTO.email} BY SESSION " }
        val user = userDTO.id?.let { this.userService.getById(it) } ?: throw ForbiddenException("You cannot get your profile")
        return if (user is Athlete){
            this.userMapper.toDTO(user);
        } else{
            this.userMapper.toDTO(user as Trainer);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/athlete")
    fun updateAthlete(@AuthenticationPrincipal userDTO: UserDTO, @RequestBody athleteDTO: AthleteDTO){
        log.info { "PUT ATHLETE ${userDTO.email}" }
        athleteDTO.email = userDTO.email
        this.userService.updateAthlete(athleteDTO)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/trainer")
    fun updateTrainer(@AuthenticationPrincipal userDTO: UserDTO, @RequestBody trainerDTO: TrainerDTO) {
        log.info { "PUT TRAINER ${userDTO.email}" }
        trainerDTO.email = userDTO.email
        this.userService.updateTrainer(trainerDTO)
    }
}
