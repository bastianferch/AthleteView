package ase.athlete_view.domain.activity.controller

import ase.athlete_view.domain.activity.pojo.dto.PlannedActivityDTO
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.user.pojo.dto.UserDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RequestMapping("api/activity")
@RestController
class ActivityController(private val activityService: ActivityService) {

    private val logger = KotlinLogging.logger {}

    @PostMapping("/planned")

    fun createPlannedActivity(@RequestBody plannedActivityDTO: PlannedActivityDTO): PlannedActivityDTO {
        logger.info { "POST PLANNED ACTIVITY $plannedActivityDTO" }
        val principal = SecurityContextHolder.getContext().authentication.principal
        return this.activityService.createPlannedActivity(plannedActivityDTO.toEntity(), principal as UserDto).toDTO()
    }
}
