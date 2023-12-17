package ase.athlete_view.domain.time_constraint.service

import ase.athlete_view.domain.time_constraint.pojo.dto.TimeConstraintDto
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.User

interface TimeConstraintService {

    fun save(timeConstraint: TimeConstraintDto, userDto: UserDTO): TimeConstraintDto

    fun edit(timeConstraint: TimeConstraintDto, userDto: UserDTO): TimeConstraintDto

    fun delete(timeConstraintId: Long, userDto: UserDTO)

    fun getById(timeConstraintId: Long, userDto: UserDTO): TimeConstraintDto

    fun getAll(userDto: UserDTO, type: String, from:String, until:String): List<TimeConstraintDto>

    fun createDefaultTimeConstraintsForUser(user: User)

    }