package ase.athlete_view.domain.user.service.mapper

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.authentication.dto.AthleteRegistrationDTO
import ase.athlete_view.domain.authentication.dto.TrainerRegistrationDTO
import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
abstract class UserMapper {

    @Mapping(source = "country", target = ".", ignore = true)
    @Mapping(source = "zip", target = ".", ignore = true)
    abstract fun toDTO(user:User): UserDto

    @Mapping(target = "activities", expression = "java(getEmptyList())")
    abstract fun toEntity(athleteRegistrationDTO: AthleteRegistrationDTO): Athlete

    @Mapping(source = "code", target = "code")
    @Mapping(source = "athletes", target = "athletes")
    abstract fun toEntity(trainerRegistrationDTO: TrainerRegistrationDTO, code: String, athletes: List<Athlete>): Trainer

    // used by the mappings. since the DTOs don't have notifications, provide an empty list
    fun getEmptyList(): List<PlannedActivity> {
        return listOf();
    }

}
