package ase.athlete_view.domain.fitness.controller

import ase.athlete_view.common.exception.entity.ForbiddenException
import ase.athlete_view.domain.fitness.service.FitnessService
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/fitness")
class FitnessController(private val fitnessService: FitnessService) {
    private val logger = KotlinLogging.logger {}

    @GetMapping()
    fun getFitness(@AuthenticationPrincipal byUser: UserDTO, @RequestParam targetUserId: Long): List<Int> {
        logger.info { "GET | getFitness() FROM $targetUserId FOR ${byUser.name}" }
        if (byUser.id == null){
            throw ForbiddenException("You do not have access to the fitness.")
        }
        return this.fitnessService.calculate(byUser.id!!, targetUserId)
    }

}