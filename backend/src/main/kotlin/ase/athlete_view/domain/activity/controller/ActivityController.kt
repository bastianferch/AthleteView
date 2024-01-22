package ase.athlete_view.domain.activity.controller

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.activity.pojo.dto.ActivityDTO
import ase.athlete_view.domain.activity.pojo.dto.CommentDTO
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
    private val log = KotlinLogging.logger {}

    @PostMapping("/planned")
    fun createPlannedActivity(
        authentication: Authentication,
        @RequestBody plannedActivityDTO: PlannedActivityDTO
    ): PlannedActivityDTO {
        log.info { "POST | createPlannedActivity($plannedActivityDTO)" }

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
        log.info { "GET | getPlannedActivity($id)" }

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
        log.info { "GET | getAllPlannedActivities($startTime, $endTime)" }

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
        log.info { "PUT | updatePlannedActivity($id, $plannedActivityDTO)" }

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
    ): Long? {
        log.info { "POST | handleFileUpload()" }
        if (files.isEmpty()) {
            throw HttpServerErrorException(HttpStatus.BAD_REQUEST)
        }

        val uid = (authentication.principal as UserDTO).id
        if (uid === null) {
            throw BadCredentialsException("Not logged in!")

        }
        return activityService.importActivity(files, uid).id

    }

    @GetMapping("/finished")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    fun fetchActivitiesForUser(
        @AuthenticationPrincipal user: UserDTO,
        @RequestParam(required = false, name = "startTime") startTime: String?,
        @RequestParam(required = false, name = "endTime") endTime: String?
    ): List<ActivityDTO> {
        log.info { "GET | fetchActivitiesForUser($startTime, $endTime)" }

        var startDate: LocalDateTime? = null
        var endDate: LocalDateTime? = null

        if (startTime != null) {
            startDate = parseStringIntoLocalDateTime(startTime)
        }

        if (endTime != null) {
            endDate = parseStringIntoLocalDateTime(endTime)
        }


        log.debug { "Start and end were provided: ${startDate.toString()}\t${endDate.toString()}" }
        // can be sure that logged-in user has an id
        val result = activityService.getAllActivities(user.id!!, startDate, endDate)
        log.debug { "Fetched activities: $result" }
        val mapped = result.map { it.toDTO() }
        log.debug { "After DTO-mapping, activities: $mapped"}
        return mapped
    }

    @GetMapping("/templates")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    fun fetchTemplatesForUser(
        @AuthenticationPrincipal user: UserDTO,
    ): List<PlannedActivityDTO> {
        log.info { "GET | fetchTemplatesForUser()" }
        // can be sure that logged-in user has an id
        val result = activityService.getAllTemplates(user.id!!)
        log.debug { "Fetched activities: $result" }
        val mapped = result.map { it.toDTO() }
        log.debug { "After DTO-mapping, activities: $mapped"}
        return mapped
    }



    @GetMapping("/finished/{activityId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    fun getFinishedActivity(@AuthenticationPrincipal user: UserDTO, @PathVariable activityId: Long): ActivityDTO {
        log.info { "GET | getFinishedActivity($activityId)" }
        if (user.id == null) {
            throw NotFoundException("Invalid credentials")
        }

        return activityService.getSingleActivityForUser(user.id!!, activityId).toDTO()
    }


    @PatchMapping("/finished/{activityId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    fun commentFinishedActivity(
        @AuthenticationPrincipal user: UserDTO,
        @PathVariable activityId: Long,
        @RequestBody comment: CommentDTO): CommentDTO {
        log.info { "PATCH | commentFinishedActivity($activityId, $comment)" }
        if (user.id == null) {
            throw NotFoundException("Invalid credentials")
        }

        return activityService.commentActivityWithUser(user.id!!, activityId, comment).toDTO()
    }

    @PatchMapping("/finished/rate/{activityId}/{rating}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    fun rateFinishedActivity(
        @AuthenticationPrincipal user: UserDTO,
        @PathVariable activityId: Long,
        @PathVariable rating: Int): Unit {
        log.info { "PATCH | rateFinishedActivity($activityId, $rating)" }
        if (user.id == null) {
            throw NotFoundException("Invalid credentials")
        }

        activityService.rateActivityWithUser(user.id!!, activityId, rating)
    }



    private fun parseStringIntoLocalDateTime(strInput: String): LocalDateTime {
        log.trace { "C | parseStringIntoLocalDateTime($strInput)" }
        return LocalDateTime.parse(strInput, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}
