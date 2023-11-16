package ase.athlete_view.domain.time_constraint.controller

import ase.athlete_view.domain.time_constraint.pojo.dto.TimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.entity.TimeConstraint
import ase.athlete_view.domain.time_constraint.pojo.entity.WeeklyTimeConstraint
import ase.athlete_view.domain.time_constraint.service.TimeConstraintService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.security.RolesAllowed
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/constraints")
class TimeConstraintController(private val timeConstraintService: TimeConstraintService) {

    private val logger = KotlinLogging.logger {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun post(@RequestBody constraint: TimeConstraintDto): TimeConstraintDto {

        return (timeConstraintService.save(constraint))
    }

    @GetMapping
    fun getWeeklies(@RequestParam type: String): List<TimeConstraintDto>{

        logger.info { type }
        return timeConstraintService.getWeeklies()
    }
}