package ase.athlete_view.domain.activity.service

import org.springframework.web.multipart.MultipartFile

interface ActivityService {
    fun importActivity(files: List<MultipartFile>): Array<String>
}
