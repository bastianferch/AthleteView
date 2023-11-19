package ase.athlete_view.domain.activity.controller

import ase.athlete_view.domain.activity.dto.PlannedActivityDTO
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.activity.service.mapper.PlannedActivityMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("api/activity")
@RestController
class ActivityController(private val activityService: ActivityService, private val plannedActivityMapper: PlannedActivityMapper) {

    private val logger = KotlinLogging.logger {}

    @PostMapping("/planned")
    fun createPlannedActivity(plannedActivityDTO: PlannedActivityDTO): PlannedActivityDTO {
        logger.info { "POST PLANNED ACTIVITY $plannedActivityDTO" }

        return this.plannedActivityMapper.toDTO(this.activityService.createPlannedActivity(this.plannedActivityMapper.toEntity(plannedActivityDTO)))
    }
}
