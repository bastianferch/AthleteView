package ase.athlete_view.domain.time_constraint.service.impl

import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.time_constraint.persistence.DailyTimeConstraintRepository
import ase.athlete_view.domain.time_constraint.persistence.TimeConstraintRepository
import ase.athlete_view.domain.time_constraint.persistence.WeeklyTimeConstraintRepository
import ase.athlete_view.domain.time_constraint.pojo.dto.DailyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.dto.TimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.dto.WeeklyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.entity.DailyTimeConstraint
import ase.athlete_view.domain.time_constraint.pojo.entity.TimeConstraint
import ase.athlete_view.domain.time_constraint.pojo.entity.WeeklyTimeConstraint
import ase.athlete_view.domain.time_constraint.service.TimeConstraintService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class TimeConstraintServiceImpl(
    private val timeConstraintRepository: TimeConstraintRepository,
    private val weeklyTimeConstraintRepository: WeeklyTimeConstraintRepository,
    private val dailyTimeConstraintRepository: DailyTimeConstraintRepository
): TimeConstraintService {

    override fun save(timeConstraint: TimeConstraintDto): TimeConstraintDto {
        validate(timeConstraint.toEntity())
        return (timeConstraintRepository.save(timeConstraint.toEntity())).toDto()
    }

    override fun edit(timeConstraint: TimeConstraintDto): TimeConstraintDto {
        validate(timeConstraint.toEntity())
        return (timeConstraintRepository.save(timeConstraint.toEntity())).toDto()
    }

    override fun delete(timeConstraintDto: TimeConstraintDto) {
        timeConstraintRepository.delete(timeConstraintDto.toEntity())
    }

    override fun getWeeklies(): List<TimeConstraintDto> {
        return weeklyTimeConstraintRepository.findAll().map { constraint -> constraint.toDto() }
    }

    override fun getDailies(): List<TimeConstraintDto> {
        return dailyTimeConstraintRepository.findAll().map { constraint -> constraint.toDto() }
    }

    override fun getAll(): List<TimeConstraintDto> {
        val list: List<TimeConstraint> =
            weeklyTimeConstraintRepository.findAll() + dailyTimeConstraintRepository.findAll()
        return list.map { timeConstraints ->  timeConstraints.toDto() }
    }

    override fun getAllAsDailies(startOfWeek: LocalDate): List<DailyTimeConstraintDto> {
        val list: List<DailyTimeConstraint> = dailyTimeConstraintRepository.findAll() +
                weeklyTimeConstraintRepository.findAll().map { weekly -> weekly.toDaily(startOfWeek) }
        return list.map { constraint -> constraint.toDto() }
    }

    override fun getAllAsWeeklies(startTime: LocalDateTime, endTime: LocalDateTime): List<WeeklyTimeConstraintDto> {
        val list: List<WeeklyTimeConstraint> = weeklyTimeConstraintRepository.findAll() +
                dailyTimeConstraintRepository.findByStartTimeLessThanEqualEndTimeGreaterThanEqual(startTime, endTime)
                .map { daily -> daily.toWeekly()}
        return list.map { constraint -> constraint.toDto()}
    }

    private fun validate(constraint: TimeConstraint) {

        when (constraint){
            is WeeklyTimeConstraint -> {
                if (constraint.constraint.startTime.isAfter(constraint.constraint.endTime)) throw ValidationException("Start time cannot be before end time")
            }
            is DailyTimeConstraint -> {
                if (constraint.startTime.isAfter(constraint.endTime)) throw ValidationException("Start time cannot be before end time")
            }
        }


    }
}