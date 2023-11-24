package ase.athlete_view.domain.time_constraint.service

import ase.athlete_view.domain.time_constraint.pojo.dto.DailyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.dto.TimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.dto.WeeklyTimeConstraintDto
import ase.athlete_view.domain.user.pojo.dto.UserDto
import java.time.LocalDate
import java.time.LocalDateTime

interface TimeConstraintService {

    fun save(timeConstraint: TimeConstraintDto, userDto: UserDto): TimeConstraintDto

    fun edit(timeConstraint: TimeConstraintDto, userDto: UserDto): TimeConstraintDto

    fun delete(timeConstraintId: Long, userDto: UserDto)

    fun getAll(userDto: UserDto, type: String, from:String, until:String): List<TimeConstraintDto>

    fun getAllAsDailies(startOfWeek: LocalDate, userDto: UserDto): List<DailyTimeConstraintDto>

    fun getAllAsWeeklies(startTime: LocalDateTime, endTime: LocalDateTime, userDto: UserDto): List<WeeklyTimeConstraintDto>
}