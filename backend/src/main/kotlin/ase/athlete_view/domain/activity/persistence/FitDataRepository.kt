package ase.athlete_view.domain.activity.persistence

import org.springframework.web.multipart.MultipartFile
import ase.athlete_view.domain.activity.pojo.dto.FitData

interface FitDataRepository {
    fun saveFitData(data: MultipartFile): String
    fun getFitData(id: String): FitData
}
