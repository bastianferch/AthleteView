package ase.athlete_view.domain.activity.controller

import ase.athlete_view.domain.activity.service.ActivityService
import lombok.AllArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("api/activity")
@AllArgsConstructor
class ActivityController(
    val activityService: ActivityService
) {
    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    fun handleFileUpload(
        @RequestParam("files") files: List<MultipartFile>
    ): Boolean {
        if (files.isEmpty()) {
            return false
        }

        activityService.importActivity(files)
        return true
    }

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    fun doNothing(): Boolean {
        println("Reached")
        return true
    }
}
