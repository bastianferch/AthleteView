package ase.athlete_view.domain.time_constraint.service

import ase.athlete_view.domain.time_constraint.pojo.dto.TimeConstraintDto
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.User

interface TimeConstraintService {

    /**
     * saves time constraint of user into the database
     * @throws ValidationException if start and end time are not valid
     */
    fun save(timeConstraint: TimeConstraintDto, userDto: UserDTO): TimeConstraintDto

    /**
     * updates time constraint of given user
     * @throws ValidationException if  start and end time are not valid
     */
    fun edit(timeConstraint: TimeConstraintDto, userDto: UserDTO): TimeConstraintDto

    /**
     * deletes time constraint with given id of given user
     */
    fun delete(timeConstraintId: Long, userDto: UserDTO)

    /**
     * returns time constraint with given id
     */
    fun getById(timeConstraintId: Long, userDto: UserDTO): TimeConstraintDto

    /**
     * returns list of time constraints from given user
     * @param type ["daily"|"weekly"] can specify type of time constraints (DailyTimeConstraint|WeeklyTimeConstraint)
     * @param from specifies earliest start time of constraints to fetch
     * @param until specifies latest end time of constraints to fetch
     * on type "daily" creates new weekly time constraint object if specified time is greater than 1 week
     */
    fun getAll(userDto: UserDTO, type: String, from:String, until:String): List<TimeConstraintDto>

    /**
     * creates default time constraints for newly created user
     * default time constraints are 7 weekly whitelist constraints from 07:00 to 22:00
     */
    fun createDefaultTimeConstraintsForUser(user: User)

    }