package ase.athlete_view.domain.time_constraint.controller

import ase.athlete_view.domain.time_constraint.pojo.dto.DailyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.dto.TimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.dto.WeeklyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.service.TimeConstraintService
import ase.athlete_view.domain.user.pojo.dto.UserDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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

    @PostMapping("/dailies")
    @ResponseStatus(HttpStatus.CREATED)
    fun postDaily(@RequestBody constraint: DailyTimeConstraintDto, @AuthenticationPrincipal userDto: UserDto): TimeConstraintDto {

        logger.info { "POST time constraint by ${userDto.name}" }
        return (timeConstraintService.save(constraint, userDto))
    }

    @PostMapping("/weeklies")
    @ResponseStatus(HttpStatus.CREATED)
    fun postWeekly(@RequestBody constraint: WeeklyTimeConstraintDto, @AuthenticationPrincipal userDto: UserDto): TimeConstraintDto {

        logger.info { "POST time constraint by ${userDto.name}" }
        return (timeConstraintService.save(constraint, userDto))
    }

    @GetMapping
    fun getConstraints(@RequestParam(defaultValue = "") type: String,
                       @AuthenticationPrincipal userDto: UserDto,
                       @RequestParam(defaultValue = "") from: String,
                       @RequestParam(defaultValue = "") until: String
                       ): List<TimeConstraintDto> {

        logger.info { "GET Constraints" }
        return timeConstraintService.getAll(userDto, type, from, until)

    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, @AuthenticationPrincipal userDto: UserDto) {
        timeConstraintService.delete(id, userDto)
    }

}