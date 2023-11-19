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
import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.domain.user.service.mapper.UserMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class TimeConstraintServiceImpl(
    private val timeConstraintRepository: TimeConstraintRepository,
    private val weeklyTimeConstraintRepository: WeeklyTimeConstraintRepository,
    private val dailyTimeConstraintRepository: DailyTimeConstraintRepository,
    private val userService: UserService
): TimeConstraintService {

    private val logger = KotlinLogging.logger {}

    override fun save(timeConstraint: TimeConstraintDto, userDto: UserDto): TimeConstraintDto {
        timeConstraint.user = userService.getById(userDto.id!!)
        validate(timeConstraint.toEntity())
        return (timeConstraintRepository.save(timeConstraint.toEntity())).toDto()
    }

    override fun edit(timeConstraint: TimeConstraintDto, userDto: UserDto): TimeConstraintDto {
        timeConstraint.user = userService.getById(userDto.id!!)
        validate(timeConstraint.toEntity())
        return (timeConstraintRepository.save(timeConstraint.toEntity())).toDto()
    }

    override fun delete(timeConstraint: TimeConstraintDto, userDto: UserDto) {
        timeConstraint.user = userService.getById(userDto.id!!)
        timeConstraintRepository.delete(timeConstraint.toEntity())
    }

    //override fun getWeeklies(): List<TimeConstraintDto> {
    //    return weeklyTimeConstraintRepository.findByUser(user).map { constraint -> constraint.toDto() }
    //}
//
    //override fun getDailies(): List<TimeConstraintDto> {
    //    return dailyTimeConstraintRepository.findByUser(user).map { constraint -> constraint.toDto() }
    //}

    override fun getAll(userDto: UserDto, type: String, from: String): List<TimeConstraintDto> {
        val user = userService.getById(userDto.id!!)
        val list: List<TimeConstraint>

        if (from == "") {
            list = weeklyTimeConstraintRepository.findByUser(user) + dailyTimeConstraintRepository.findByUser(user)
        } else {
            val date = LocalDateTime.parse(from)
            list = weeklyTimeConstraintRepository.findByUser(user) +
                    dailyTimeConstraintRepository.findByUserAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                        user,
                        date,
                        date.plusDays(7)
                    )
        }

        return list.map { timeConstraints ->  timeConstraints.toDto() }
    }

    override fun getAllAsDailies(startOfWeek: LocalDate, userDto: UserDto): List<DailyTimeConstraintDto> {
        val user = userService.getById(userDto.id!!)
        val list: List<DailyTimeConstraint> = dailyTimeConstraintRepository.findByUser(user) +
                weeklyTimeConstraintRepository.findByUser(user).map { weekly -> weekly.toDaily(startOfWeek) }
        return list.map { constraint -> constraint.toDto() }
    }

    override fun getAllAsWeeklies(startTime: LocalDateTime, endTime: LocalDateTime, userDto: UserDto): List<WeeklyTimeConstraintDto> {
        val user = userService.getById(userDto.id!!)
        val list: List<WeeklyTimeConstraint> = weeklyTimeConstraintRepository.findByUser(user) +
                dailyTimeConstraintRepository.findByUserAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(userService.getById(userDto.id!!), startTime, endTime)
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