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

    fun getById(timeConstraintId: Long, userDto: UserDto): TimeConstraintDto

    fun getAll(userDto: UserDto, type: String, from:String, until:String): List<TimeConstraintDto>

    }