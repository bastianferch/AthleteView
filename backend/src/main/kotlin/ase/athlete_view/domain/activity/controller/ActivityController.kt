package ase.athlete_view.domain.activity.controller

import ase.athlete_view.domain.activity.pojo.dto.PlannedActivityDTO
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.user.pojo.dto.UserDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RequestMapping("api/activity")
@RestController
class ActivityController(private val activityService: ActivityService) {

    private val logger = KotlinLogging.logger {}

    @PostMapping("/planned")
    fun createPlannedActivity(
        authentication: Authentication,
        @RequestBody plannedActivityDTO: PlannedActivityDTO
    ): PlannedActivityDTO {
        logger.info { "POST PLANNED ACTIVITY $plannedActivityDTO" }

        val userId = (authentication.principal as UserDto).id
        if (userId != null) {
            return this.activityService.createPlannedActivity(plannedActivityDTO.toEntity(), userId).toDTO()
        }
        // TODO is this the right thing to throw here?
        throw BadCredentialsException("")
    }


    @GetMapping("/planned/{id}")
    fun getPlannedActivity(
        authentication: Authentication,
        @PathVariable id: Long
    ): PlannedActivityDTO {
        logger.info { "GET PLANNED ACTIVITY $id" }

        val userId = (authentication.principal as UserDto).id
        if (userId != null) {
            return this.activityService.getPlannedActivity(id, userId).toDTO()
        }
        // TODO is this the right thing to throw here?
        throw BadCredentialsException("")
    }

    @GetMapping("/planned/")
    fun getAllPlannedActivities(authentication: Authentication): List<PlannedActivityDTO> {
        logger.info { "GET ALL PLANNED ACTIVITIES" }

        val userId = (authentication.principal as UserDto).id
        if (userId != null) {
            return this.activityService.getAllPlannedActivities(userId).map { it.toDTO() }
        }
        // TODO is this the right thing to throw here?
        throw BadCredentialsException("")
    }

    @PutMapping("/planned/{id}")
    fun updatePlannedActivity(
        authentication: Authentication,
        @PathVariable id: Long,
        @RequestBody plannedActivityDTO: PlannedActivityDTO
    ): PlannedActivityDTO {
        logger.info { "PUT PLANNED ACTIVITY $plannedActivityDTO" }

        val userId = (authentication.principal as UserDto).id
        if (userId != null) {
            return this.activityService.updatePlannedActivity(id, plannedActivityDTO.toEntity(), userId).toDTO()
        }
        // TODO is this the right thing to throw here?
        throw BadCredentialsException("")

    }
}
