package ase.athlete_view.domain.activity.controller

import ase.athlete_view.domain.activity.pojo.dto.PlannedActivityDTO
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("api/activity")
class ActivityController(private val activityService: ActivityService) {

    private val logger = KotlinLogging.logger {}

    @PostMapping("/planned")
    fun createPlannedActivity(
        authentication: Authentication,
        @RequestBody plannedActivityDTO: PlannedActivityDTO
    ): PlannedActivityDTO {
        logger.info { "POST PLANNED ACTIVITY $plannedActivityDTO ${authentication.principal}" }

        val userId = (authentication.principal as UserDTO).id
        if (userId != null) {
            return this.activityService.createPlannedActivity(plannedActivityDTO.toEntity(), userId).toDTO()
        }
        throw BadCredentialsException("")
    }


    @GetMapping("/planned/{id}")
    fun getPlannedActivity(
        authentication: Authentication,
        @PathVariable id: Long
    ): PlannedActivityDTO {
        logger.info { "GET PLANNED ACTIVITY $id" }

        val userId = (authentication.principal as UserDTO).id
        if (userId != null) {
            return this.activityService.getPlannedActivity(id, userId).toDTO()
        }
        throw BadCredentialsException("Not logged in!")
    }

    @GetMapping("/planned/")
    fun getAllPlannedActivities(authentication: Authentication): List<PlannedActivityDTO> {
        logger.info { "GET ALL PLANNED ACTIVITIES" }

        val userId = (authentication.principal as UserDTO).id
        if (userId != null) {
            return this.activityService.getAllPlannedActivities(userId).map { it.toDTO() }
        }
        throw BadCredentialsException("Not logged in!")
    }

    @PutMapping("/planned/{id}")
    fun updatePlannedActivity(
        authentication: Authentication,
        @PathVariable id: Long,
        @RequestBody plannedActivityDTO: PlannedActivityDTO
    ): PlannedActivityDTO {
        logger.info { "PUT PLANNED ACTIVITY $plannedActivityDTO" }

        val userId = (authentication.principal as UserDTO).id
        if (userId != null) {
            return this.activityService.updatePlannedActivity(id, plannedActivityDTO.toEntity(), userId).toDTO()
        }
        throw BadCredentialsException("Not logged in!")

    }
    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    fun handleFileUpload(
        authentication: Authentication,
        @RequestParam("files") files: List<MultipartFile>
    ): Unit {
        logger.info { "File import detected" }
        if (files.isEmpty()) {
            throw HttpServerErrorException(HttpStatus.BAD_REQUEST)
        }

        val uid = (authentication.principal as UserDTO).id
        if (uid === null) {
            throw BadCredentialsException("Not logged in!")

        }
        activityService.importActivity(files, uid)
    }
}
