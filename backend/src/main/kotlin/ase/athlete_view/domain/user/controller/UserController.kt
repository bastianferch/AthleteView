package ase.athlete_view.domain.user.controller

import ase.athlete_view.common.exception.entity.ForbiddenException
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import ase.athlete_view.domain.user.pojo.dto.PreferencesDTO
import ase.athlete_view.domain.user.pojo.dto.TrainerDTO
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.service.AthleteService
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.domain.user.service.mapper.UserMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/user")
class UserController(
    private val userService: UserService, private val athleteService: AthleteService,
    private val userMapper: UserMapper
) {
    val log = KotlinLogging.logger {}

    @ResponseStatus(HttpStatus.OK)
    @GetMapping()
    fun get(@AuthenticationPrincipal userDTO: UserDTO): UserDTO {
        log.info { "GET USER ${userDTO.email} BY SESSION " }
        val user = userDTO.id?.let { this.userService.getById(it) } ?: throw ForbiddenException("You cannot get your profile")
        return if (user is Athlete) {
            this.userMapper.toDTO(user)
        } else {
            this.userMapper.toDTO(user as Trainer)
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/athlete")
    fun getAthletes(@AuthenticationPrincipal userDTO: UserDTO): List<AthleteDTO> {
        log.info { "GET ATHLETES FOR ${userDTO.email} BY SESSION " }


        val athleteDtos = mutableListOf<AthleteDTO>()
        val athletes = userDTO.id?.let { this.athleteService.getByTrainerId(it) }
        if (athletes != null) {
            for (i in athletes.indices) {
                athleteDtos.add(athletes[i].toAthleteDto())
            }
        }
        return athleteDtos
    }


    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/athlete")
    fun updateAthlete(@AuthenticationPrincipal userDTO: UserDTO, @RequestBody athleteDTO: AthleteDTO) {
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

    @GetMapping("/preferences")
    fun getPreferences(@AuthenticationPrincipal user: UserDTO): PreferencesDTO? {
        log.info { "GET PREFERENCES ${user.id}" }
        return this.userService.getPreferences(user)?.toDTO()
    }

    @PatchMapping("/preferences")
    fun patchPreferences(@AuthenticationPrincipal user: UserDTO, @RequestBody preferencesDTO: PreferencesDTO): PreferencesDTO? {
        log.info { "PATCH PREFERENCES ${user.id} $preferencesDTO" }
        return this.userService.patchPreferences(user, preferencesDTO)?.toDTO()
    }
}
