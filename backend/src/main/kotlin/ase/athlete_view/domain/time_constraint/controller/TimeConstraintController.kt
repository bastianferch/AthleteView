package ase.athlete_view.domain.time_constraint.controller

import ase.athlete_view.domain.time_constraint.pojo.dto.DailyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.dto.TimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.dto.WeeklyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.service.TimeConstraintService
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/constraints")
class TimeConstraintController(private val timeConstraintService: TimeConstraintService) {

    private val logger = KotlinLogging.logger {}

    @PostMapping("/dailies")
    @ResponseStatus(HttpStatus.CREATED)
    fun postDaily(@RequestBody constraint: DailyTimeConstraintDto, @AuthenticationPrincipal userDto: UserDTO): TimeConstraintDto {

        logger.info { "POST time constraint by ${userDto.name}" }
        return (timeConstraintService.save(constraint, userDto))
    }

    @PostMapping("/weeklies")
    @ResponseStatus(HttpStatus.CREATED)
    fun postWeekly(@RequestBody constraint: WeeklyTimeConstraintDto, @AuthenticationPrincipal userDto: UserDTO): TimeConstraintDto {

        logger.info { "POST time constraint by ${userDto.name}" }
        return (timeConstraintService.save(constraint, userDto))
    }

    @PutMapping("/dailies/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    fun putDaily(@RequestBody constraint: DailyTimeConstraintDto, @PathVariable id: Long, @AuthenticationPrincipal userDto: UserDTO): TimeConstraintDto {

        logger.info { "PUT time constraint by ${userDto.name}" }
        return (timeConstraintService.edit(constraint, userDto))
    }

    @PutMapping("/weeklies/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun putWeekly(@RequestBody constraint: WeeklyTimeConstraintDto, @PathVariable id: Long, @AuthenticationPrincipal userDto: UserDTO): TimeConstraintDto {

        logger.info { "PUT time constraint by ${userDto.name}" }
        return (timeConstraintService.edit(constraint, userDto))
    }

    @GetMapping
    fun getConstraints(@RequestParam(defaultValue = "") type: String,
                       @AuthenticationPrincipal userDto: UserDTO,
                       @RequestParam(defaultValue = "") from: String,
                       @RequestParam(defaultValue = "") until: String
                       ): List<TimeConstraintDto> {

        logger.info { "GET Constraints" }
        return timeConstraintService.getAll(userDto, type, from, until)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long, @AuthenticationPrincipal userDto: UserDTO): TimeConstraintDto {
        logger.info { "GET constraint with id $id by ${userDto.name}" }
        return timeConstraintService.getById(id, userDto)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, @AuthenticationPrincipal userDto: UserDTO) {
        logger.info { "DELETE time constraint with id $id by ${userDto.name}"}
        timeConstraintService.delete(id, userDto)
    }

}