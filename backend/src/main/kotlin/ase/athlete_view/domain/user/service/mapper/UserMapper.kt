package ase.athlete_view.domain.user.service.mapper

import ase.athlete_view.domain.authentication.dto.RegistrationDTO
import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.domain.user.pojo.entity.User
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface UserMapper {

    fun toDTO(user:User): UserDto
    fun toEntity(userDto: UserDto): User
    fun toEntity(registrationDTO: RegistrationDTO): User
}