package ase.athlete_view.domain.health.service.mapper

import ase.athlete_view.domain.health.pojo.dto.HealthDTO
import ase.athlete_view.domain.health.pojo.entity.Health
import ase.athlete_view.domain.user.pojo.entity.User
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface HealthMapper {
    fun toDto(entity: Health): HealthDTO
    fun toEntity(dto: HealthDTO): Health
    @Mapping(target = "user", source = "user")
    @Mapping(target = "id", source = "dto", ignore = true)
    fun toEntity(dto: HealthDTO, user: User): Health
}