package ase.athlete_view.domain.activity.controller

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.activity.pojo.dto.ActivityDTO
import ase.athlete_view.domain.activity.pojo.dto.PlannedActivityDTO
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    @GetMapping("/planned")
    fun getAllPlannedActivities(
        authentication: Authentication,
        @RequestParam(required=false, name = "startTime") startTime: String?,
        @RequestParam(required=false, name = "endTime") endTime: String?
    ): List<PlannedActivityDTO> {
        logger.info { "GET ALL PLANNED ACTIVITIES" }

        var startDate: LocalDateTime? = null
        var endDate: LocalDateTime? = null

        if (startTime != null) {
            startDate = parseStringIntoLocalDateTime(startTime)
        }

        if (endTime != null) {
            endDate = parseStringIntoLocalDateTime(endTime)
        }

        val userId = (authentication.principal as UserDTO).id
        if (userId != null) {
            return this.activityService.getAllPlannedActivities(userId, startDate, endDate).map { it.toDTO() }
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

    @GetMapping("/finished")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    fun fetchActivitiesForUser(
        @AuthenticationPrincipal user: UserDTO,
        @RequestParam(required = false, name = "startTime") startTime: String?,
        @RequestParam(required = false, name = "endTime") endTime: String?
    ): List<ActivityDTO> {
        logger.info { "Fetching finished/imported activities for user ${user.id}" }

        var startDate: LocalDateTime? = null
        var endDate: LocalDateTime? = null

        if (startTime != null) {
            startDate = parseStringIntoLocalDateTime(startTime)
        }

        if (endTime != null) {
            endDate = parseStringIntoLocalDateTime(endTime)
        }


        logger.debug { "Start and end were provided: ${startDate.toString()}\t${endDate.toString()}" }
        // can be sure that logged-in user has an id
        val result = activityService.getAllActivities(user.id!!, startDate, endDate)
        logger.debug { "Fetched activities: $result" }
        val mapped = result.map { it.toDTO() }
        logger.debug { "After DTO-mapping, activities: $mapped"}
        return mapped
    }

    @GetMapping("/finished/{activityId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    fun getFinishedActivity(@AuthenticationPrincipal user: UserDTO, @PathVariable activityId: Long): ActivityDTO {
        logger.info { "C | getFinishedActivity($activityId)" }
        if (user.id == null) {
            throw NotFoundException("Invalid credentials")
        }

        return activityService.getSingleActivityForUser(user.id!!, activityId).toDTO()
    }

    private fun parseStringIntoLocalDateTime(strInput: String): LocalDateTime {
        return LocalDateTime.parse(strInput, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}
