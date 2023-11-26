package ase.athlete_view.domain.user.service.mapper

import ase.athlete_view.domain.authentication.dto.AthleteRegistrationDTO
import ase.athlete_view.domain.authentication.dto.TrainerRegistrationDTO
import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface UserMapper {

    @Mapping(source = "country", target = ".", ignore = true)
    @Mapping(source = "zip", target = ".", ignore = true)
    fun toDTO(user:User): UserDto

    fun toEntity(athleteRegistrationDTO: AthleteRegistrationDTO): Athlete

    @Mapping(source = "code", target = "code")
    @Mapping(source = "athletes", target = "athletes")
    fun toEntity(trainerRegistrationDTO: TrainerRegistrationDTO, code: String, athletes: List<Athlete>): Trainer


}