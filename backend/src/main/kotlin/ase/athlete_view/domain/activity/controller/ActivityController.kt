package ase.athlete_view.domain.activity.controller

import ase.athlete_view.domain.activity.pojo.dto.PlannedActivityDTO
import ase.athlete_view.domain.activity.service.ActivityService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.*

@RequestMapping("api/activity")
@RestController
class ActivityController(private val activityService: ActivityService) {

    private val logger = KotlinLogging.logger {}

    @PostMapping("/planned")
    fun createPlannedActivity(@RequestBody plannedActivityDTO: PlannedActivityDTO): PlannedActivityDTO {
        logger.info { "POST PLANNED ACTIVITY $plannedActivityDTO" }
        return this.activityService.createPlannedActivity(plannedActivityDTO.toEntity()).toDTO()
    }


    @GetMapping("/planned/{id}")
    fun getPlannedActivity(@PathVariable id: Long): PlannedActivityDTO {
        logger.info { "GET PLANNED ACTIVITY $id" }
        return this.activityService.getPlannedActivity(id).toDTO()
    }

    @PutMapping("/planned/{id}")
    fun updatePlannedActivity(@PathVariable id: Long, @RequestBody plannedActivityDTO: PlannedActivityDTO): PlannedActivityDTO {
        logger.info { "PUT PLANNED ACTIVITY $plannedActivityDTO" }
        return this.activityService.updatePlannedActivity(id, plannedActivityDTO.toEntity()).toDTO()
    }
}
