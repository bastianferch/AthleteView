package ase.athlete_view.domain.time_constraint.service

import ase.athlete_view.domain.time_constraint.pojo.dto.DailyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.dto.TimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.dto.WeeklyTimeConstraintDto
import java.time.LocalDate
import java.time.LocalDateTime

interface TimeConstraintService {

    fun save(timeConstraint: TimeConstraintDto): TimeConstraintDto

    fun edit(timeConstraint: TimeConstraintDto): TimeConstraintDto

    fun delete(timeConstraintDto: TimeConstraintDto)

    fun getWeeklies(): List<TimeConstraintDto>

    fun getDailies(): List<TimeConstraintDto>

    fun getAll(): List<TimeConstraintDto>

    fun getAllAsDailies(startOfWeek: LocalDate): List<DailyTimeConstraintDto>

    fun getAllAsWeeklies(startTime: LocalDateTime, endTime: LocalDateTime): List<WeeklyTimeConstraintDto>
}