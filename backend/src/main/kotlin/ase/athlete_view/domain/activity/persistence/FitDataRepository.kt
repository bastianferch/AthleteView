package ase.athlete_view.domain.activity.persistence

import org.springframework.web.multipart.MultipartFile

interface FitDataRepository {
    fun saveFitData(data: MultipartFile): String
}
