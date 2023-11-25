package ase.athlete_view.domain.activity.controller

import ase.athlete_view.domain.activity.service.ActivityService
import io.github.oshai.kotlinlogging.KotlinLogging
import lombok.AllArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("api/activity")
@AllArgsConstructor
class ActivityController(
    val activityService: ActivityService
) {
    private val logger = KotlinLogging.logger {}

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    fun handleFileUpload(
        @RequestParam("files") files: List<MultipartFile>
    ): Unit {
        logger.info { "File import detected" }
        if (files.isEmpty()) {
            throw HttpServerErrorException(HttpStatus.BAD_REQUEST)
        }

        //return
        activityService.importActivity(files)
    }

    @GetMapping("/sanity")
    @ResponseStatus(HttpStatus.OK)
    fun doNothing(): Unit {
        println("fick di")
    }

}
