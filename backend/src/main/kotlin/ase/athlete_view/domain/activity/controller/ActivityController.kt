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

        //TODO this endpoint should get the logged in user via the `Authentication` object!
        // Otherwise we have to rely on user-provided data for which user the activity is created.

        // TODO if this endpoint is called from an athlete, the activity can only be created for themselves
        //  if user is a trainer, the activity can only be created for athletes they train

        return this.activityService.createPlannedActivity(plannedActivityDTO.toEntity()).toDTO()
    }


    @GetMapping("/planned/{id}")
    fun getPlannedActivity(@PathVariable id: Long): PlannedActivityDTO {
        logger.info { "GET PLANNED ACTIVITY $id" }

        //TODO this endpoint should get the logged in user via the `Authentication` object!
        //  otherwise, every user can see every activity of other users

        return this.activityService.getPlannedActivity(id).toDTO()
    }

    @PutMapping("/planned/{id}")
    fun updatePlannedActivity(@PathVariable id: Long, @RequestBody plannedActivityDTO: PlannedActivityDTO): PlannedActivityDTO {
        logger.info { "PUT PLANNED ACTIVITY $plannedActivityDTO" }

        // TODO again check who requests this!

        return this.activityService.updatePlannedActivity(id, plannedActivityDTO.toEntity()).toDTO()
    }
}
