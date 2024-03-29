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
package ase.athlete_view.domain.time_constraint.service.impl

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.common.sanitization.Sanitizer
import ase.athlete_view.domain.time_constraint.persistence.DailyTimeConstraintRepository
import ase.athlete_view.domain.time_constraint.persistence.TimeConstraintRepository
import ase.athlete_view.domain.time_constraint.persistence.WeeklyTimeConstraintRepository
import ase.athlete_view.domain.time_constraint.pojo.dto.TimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.entity.DailyTimeConstraint
import ase.athlete_view.domain.time_constraint.pojo.entity.TimeConstraint
import ase.athlete_view.domain.time_constraint.pojo.entity.TimeFrame
import ase.athlete_view.domain.time_constraint.pojo.entity.WeeklyTimeConstraint
import ase.athlete_view.domain.time_constraint.service.TimeConstraintService
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
class TimeConstraintServiceImpl(
    private val timeConstraintRepository: TimeConstraintRepository,
    private val weeklyTimeConstraintRepository: WeeklyTimeConstraintRepository,
    private val dailyTimeConstraintRepository: DailyTimeConstraintRepository,
    private val userService: UserService,
    private val sanitizer: Sanitizer,
): TimeConstraintService {

    private val log = KotlinLogging.logger {}

    override fun save(timeConstraint: TimeConstraintDto, userDto: UserDTO): TimeConstraintDto {
        log.trace { "S | save($timeConstraint, $userDto)" }
        val constraint = sanitizer.sanitizeTimeConstraintDto(timeConstraint).toEntity()
        constraint.user = userService.getById(userDto.id!!)
        validate(constraint)
        return (timeConstraintRepository.save(constraint)).toDto()
    }

    override fun edit(timeConstraint: TimeConstraintDto, userDto: UserDTO): TimeConstraintDto {
        log.trace { "S | edit($timeConstraint, $userDto)" }
        val constraint = timeConstraintRepository.findByIdOrNull(timeConstraint.id) ?: throw NotFoundException("Could not find constraint by given id")
        if (constraint.user?.id != userDto.id)
            throw ValidationException("Cannot edit time constraint from different user")
        val newConstraint = sanitizer.sanitizeTimeConstraintDto(timeConstraint).toEntity()
        newConstraint.user = userService.getById(userDto.id!!)
        validate(newConstraint)
        return (timeConstraintRepository.save(newConstraint)).toDto()
    }

    override fun delete(timeConstraintId: Long, userDto: UserDTO) {
        log.trace { "S | delete($timeConstraintId, $userDto)" }
        val constraint = timeConstraintRepository.findByIdOrNull(timeConstraintId) ?: throw NotFoundException("Could not find constraint by given id")
        if (constraint.user?.id != userDto.id)
            throw ValidationException("Cannot delete time constraint from different user")
        timeConstraintRepository.deleteById(timeConstraintId)
    }

    override fun getById(timeConstraintId: Long, userDto: UserDTO): TimeConstraintDto {
        log.trace { "S | getById($timeConstraintId, $userDto)" }
        var constraint: TimeConstraint? = dailyTimeConstraintRepository.findByIdOrNull(timeConstraintId)
        if (constraint == null) constraint = weeklyTimeConstraintRepository.findByIdOrNull(timeConstraintId)?: throw NotFoundException("Could not find constraint by given id")
        if (constraint.user?.id != userDto.id)
            throw ValidationException("Cannot get time constraint from different user")
        return constraint.toDto()
    }

    override fun getAll(userDto: UserDTO, type: String, from: String, until: String): List<TimeConstraintDto> {
        log.trace { "S | getAll($userDto, $type, $from, $until)" }
        val user = userService.getById(userDto.id!!)
        var weeklies: List<TimeConstraint>
        var dailies: List<TimeConstraint>
        val list: List<TimeConstraint>
        val date: LocalDateTime
        val endTime: LocalDateTime = if (until == "") LocalDateTime.now().plusDays(7) else LocalDateTime.parse(until, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx"))

        if (from == "") {
            date = LocalDateTime.now()
            weeklies = weeklyTimeConstraintRepository.findByUser(user)
            dailies = dailyTimeConstraintRepository.findByUser(user)
        } else {
            date = LocalDateTime.parse(from, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx"))
            weeklies = weeklyTimeConstraintRepository.findByUser(user)
            dailies = dailyTimeConstraintRepository.findByUserAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(
                user,
                date,
                endTime
            )
        }

        if (type == "daily") {
            val tempList = weeklies
            weeklies = weeklies.map { weekly -> weekly.toDaily(LocalDate.from(date)) }
            if (from != "" && until != "") {
                var currentDate = date
                while (endTime.isAfter(currentDate.plusDays(7))) {
                    currentDate = currentDate.plusDays(7)
                    weeklies += (tempList.map { weekly -> weekly.toDaily(LocalDate.from(currentDate)) }).filter { daily -> daily.endTime.isBefore(endTime) }
                }
            }
        }
        if (type == "weekly") {
            dailies = dailies.map { daily -> daily.toWeekly()}
        }

        list = weeklies + dailies
        return list.map { timeConstraints ->  timeConstraints.toDto() }
    }

    override fun createDefaultTimeConstraintsForUser(user: User) {
        log.trace { "S | createDefaultTimeConstraintsForUser($user)" }
        val defaultStartTime = LocalTime.of(7,0)
        val defaultEndTime = LocalTime.of(22,0)
        val titleForDefaultConstraint = "normal training hours"
        for (day in DayOfWeek.values()) {
            val timeConstraint = WeeklyTimeConstraint(null, false, titleForDefaultConstraint, user, TimeFrame(day,defaultStartTime,defaultEndTime))
            weeklyTimeConstraintRepository.save(timeConstraint)
        }
    }

    private fun validate(constraint: TimeConstraint) {
        log.trace { "S | validate($constraint) " }
        when (constraint){
            is WeeklyTimeConstraint -> {
                if (!constraint.constraint.endTime.isAfter(constraint.constraint.startTime)) throw ValidationException("End time must be after start time")
            }
            is DailyTimeConstraint -> {
                if (!constraint.endTime.isAfter(constraint.startTime)) throw ValidationException("End time must be after start time")
            }
        }
    }
}
