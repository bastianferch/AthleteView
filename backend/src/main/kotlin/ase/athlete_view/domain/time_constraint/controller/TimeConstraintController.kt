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

    private val log = KotlinLogging.logger {}

    @PostMapping("/dailies")
    @ResponseStatus(HttpStatus.CREATED)
    fun postDaily(@RequestBody constraint: DailyTimeConstraintDto, @AuthenticationPrincipal userDto: UserDTO): TimeConstraintDto {

        log.info { "POST | postDaily($constraint)" }
        return (timeConstraintService.save(constraint, userDto))
    }

    @PostMapping("/weeklies")
    @ResponseStatus(HttpStatus.CREATED)
    fun postWeekly(@RequestBody constraint: WeeklyTimeConstraintDto, @AuthenticationPrincipal userDto: UserDTO): TimeConstraintDto {

        log.info { "POST | postWeekly($constraint)" }
        return (timeConstraintService.save(constraint, userDto))
    }

    @PutMapping("/dailies/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun putDaily(@RequestBody constraint: DailyTimeConstraintDto, @PathVariable id: Long, @AuthenticationPrincipal userDto: UserDTO): TimeConstraintDto {

        log.info { "PUT | putDaily($constraint, $id)" }
        return (timeConstraintService.edit(constraint, userDto))
    }

    @PutMapping("/weeklies/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun putWeekly(@RequestBody constraint: WeeklyTimeConstraintDto, @PathVariable id: Long, @AuthenticationPrincipal userDto: UserDTO): TimeConstraintDto {

        log.info { "PUT | putWeekly($constraint, $id)" }
        return (timeConstraintService.edit(constraint, userDto))
    }

    @GetMapping
    fun getConstraints(@RequestParam(defaultValue = "") type: String,
                       @AuthenticationPrincipal userDto: UserDTO,
                       @RequestParam(defaultValue = "") from: String,
                       @RequestParam(defaultValue = "") until: String
                       ): List<TimeConstraintDto> {

        log.info { "GET | getConstraints($type, $from, $until)" }
        return timeConstraintService.getAll(userDto, type, from, until)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long, @AuthenticationPrincipal userDto: UserDTO): TimeConstraintDto {
        log.info { "GET | getById($id)" }
        return timeConstraintService.getById(id, userDto)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, @AuthenticationPrincipal userDto: UserDTO) {
        log.info { "DELETE | delete($id)"}
        timeConstraintService.delete(id, userDto)
    }

}