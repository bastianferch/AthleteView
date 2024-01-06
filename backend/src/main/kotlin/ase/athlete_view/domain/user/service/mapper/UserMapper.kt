package ase.athlete_view.domain.user.service.mapper

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.authentication.dto.AthleteRegistrationDTO
import ase.athlete_view.domain.authentication.dto.TrainerRegistrationDTO
import ase.athlete_view.domain.notification.pojo.entity.Notification
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import ase.athlete_view.domain.user.pojo.dto.TrainerDTO
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget

@Mapper(componentModel = "spring")
abstract class UserMapper {

    @Mapping(source = "country", target = ".", ignore = true)
    @Mapping(source = "zip", target = ".", ignore = true)
    @Mapping(source = "notifications", target = ".", ignore = true)
    @Mapping(source = "password", target = "password", ignore = true)
    abstract fun toUserDTO(user: User): UserDTO

    @Mapping(target = "activities", expression = "java(getEmptyActivityList())")
    abstract fun toEntity(athleteRegistrationDTO: AthleteRegistrationDTO): Athlete

    @Mapping(source = "password", target = "password", ignore = true)
    @Mapping(target = "athletes", expression = "java(athletesFromTrainer(trainer))")
    abstract fun toTrainerDTO(trainer: Trainer): TrainerDTO

    @Mapping(source = "password", target = "password", ignore = true)
    @Mapping(target = "trainer", expression = "java(trainerFromAthlete(athlete))")
    abstract fun toAthleteDTO(athlete: Athlete): AthleteDTO


    @Mapping(source = "code", target = "code")
    @Mapping(source = "athletes", target = "athletes")
    @Mapping(source = "unacceptedAthletes", target = "unacceptedAthletes")
    abstract fun toEntity(
        trainerRegistrationDTO: TrainerRegistrationDTO,
        code: String,
        athletes: List<Athlete>,
        unacceptedAthletes: List<Athlete>
    ): Trainer

    @Mapping(source = "password", target = "password", ignore = true)
    @Mapping(source = "id", target = "id", ignore = true)
    @Mapping(source = "email", target = "email", ignore = true)
    @Mapping(source = ".", target = "athletes", ignore = true)
    abstract fun toEntity(@MappingTarget trainer: Trainer, trainerDTO: TrainerDTO)

    @Mapping(source = "password", target = "password", ignore = true)
    @Mapping(source = "id", target = "id", ignore = true)
    @Mapping(source = "email", target = "email", ignore = true)
    @Mapping(source = ".", target = "trainer", ignore = true)
    abstract fun toEntity(@MappingTarget athlete: Athlete, athleteDTO: AthleteDTO)

    // used by the mappings. since the DTOs don't have notifications, provide an empty list
    fun getEmptyNotificationList(): List<Notification> {
        return listOf();
    }

    fun getEmptyActivityList(): Set<PlannedActivity> {
        return emptySet()
    }

    // used by the mappings. Fixes stack overflow of circular dependencies.
    fun trainerFromAthlete(athlete: Athlete): TrainerDTO? {
        if (athlete.trainer != null) {
            return this.toTrainerDTO(athlete.trainer!!)
        }
        return null;
    }

    // used by the mappings. Fixes stack overflow of circular dependencies.
    // map in kotlin does mutate the original entity, therefore trainer is unset/set.
    fun athletesFromTrainer(trainer: Trainer): List<AthleteDTO> {
        return trainer.athletes.stream().map { athlete ->
            athlete.trainer = null
            val dto = toAthleteDTO(athlete)
            athlete.trainer = trainer
            return@map dto
        }.toList()
    }
}
