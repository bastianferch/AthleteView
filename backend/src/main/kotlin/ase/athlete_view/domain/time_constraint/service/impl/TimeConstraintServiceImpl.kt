package ase.athlete_view.domain.time_constraint.service.impl

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.time_constraint.persistence.DailyTimeConstraintRepository
import ase.athlete_view.domain.time_constraint.persistence.TimeConstraintRepository
import ase.athlete_view.domain.time_constraint.persistence.WeeklyTimeConstraintRepository
import ase.athlete_view.domain.time_constraint.pojo.dto.TimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.entity.DailyTimeConstraint
import ase.athlete_view.domain.time_constraint.pojo.entity.TimeConstraint
import ase.athlete_view.domain.time_constraint.pojo.entity.WeeklyTimeConstraint
import ase.athlete_view.domain.time_constraint.service.TimeConstraintService
import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.domain.user.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        val constraint = timeConstraintRepository.findByIdOrNull(timeConstraint.id) ?: throw NotFoundException("Could not find constraint by given id")
        if (constraint.user.id != userDto.id)
            throw ValidationException("Cannot edit time constraint from different user")
        timeConstraint.user = userService.getById(userDto.id!!)
        validate(timeConstraint.toEntity())
        return (timeConstraintRepository.save(timeConstraint.toEntity())).toDto()
    }

    override fun delete(timeConstraintId: Long, userDto: UserDto) {
        val constraint = timeConstraintRepository.findByIdOrNull(timeConstraintId) ?: throw NotFoundException("Could not find constraint by given id")
        if (constraint.user.id != userDto.id)
            throw ValidationException("Cannot delete time constraint from different user")
        timeConstraintRepository.deleteById(timeConstraintId)
    }

    override fun getById(timeConstraintId: Long, userDto: UserDto): TimeConstraintDto {
        var constraint: TimeConstraint? = dailyTimeConstraintRepository.findByIdOrNull(timeConstraintId)
        if (constraint == null) constraint = weeklyTimeConstraintRepository.findByIdOrNull(timeConstraintId)?: throw NotFoundException("Could not find constraint by given id")
        if (constraint.user.id != userDto.id)
            throw ValidationException("Cannot get time constraint from different user")
        return constraint.toDto()
    }

    override fun getAll(userDto: UserDto, type: String, from: String, until: String): List<TimeConstraintDto> {
        val user = userService.getById(userDto.id!!)
        var weeklies: List<TimeConstraint>
        var dailies: List<TimeConstraint>
        val list: List<TimeConstraint>
        val date: LocalDateTime
        val endTime: LocalDateTime = if (until == "") LocalDateTime.now().plusDays(7) else LocalDateTime.parse(until, DateTimeFormatter.ofPattern("d.MM.yyyy, HH:mm:ss"))

        if (from == "") {
            date = LocalDateTime.now()
            weeklies = weeklyTimeConstraintRepository.findByUser(user)
            dailies = dailyTimeConstraintRepository.findByUser(user)
        } else {
            date = LocalDateTime.parse(from, DateTimeFormatter.ofPattern("d.MM.yyyy, HH:mm:ss"))
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