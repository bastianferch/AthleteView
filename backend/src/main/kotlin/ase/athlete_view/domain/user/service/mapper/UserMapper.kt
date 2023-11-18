package ase.athlete_view.domain.user.service.mapper

import ase.athlete_view.domain.authentication.dto.RegistrationDTO
import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.domain.user.pojo.entity.User
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface UserMapper {

    @Mapping(source = "country", target = ".", ignore = true)
    @Mapping(source = "zip", target = ".", ignore = true)
    fun toDTO(user:User): UserDto
}