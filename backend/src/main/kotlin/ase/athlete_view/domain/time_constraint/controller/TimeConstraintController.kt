/*-
 * #%L
 * athlete_view
 * %%
 * Copyright (C) 2023 - 2024 TU Wien INSO ASE GROUP 5 WS2023
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
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
