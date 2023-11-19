package ase.athlete_view.domain.activity.service.mapper

import ase.athlete_view.domain.activity.dto.PlannedActivityDTO
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface PlannedActivityMapper {

    fun toEntity(plannedActivityDTO: PlannedActivityDTO): PlannedActivity

    fun toDTO(plannedActivity: PlannedActivity): PlannedActivityDTO
}