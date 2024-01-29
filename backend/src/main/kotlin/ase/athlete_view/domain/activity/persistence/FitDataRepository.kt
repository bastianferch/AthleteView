package ase.athlete_view.domain.activity.persistence

import ase.athlete_view.domain.activity.pojo.dto.FitData
import java.io.InputStream

interface FitDataRepository {
    fun getFitData(id: String): FitData
    fun saveFitData(data: InputStream, filename: String, uid: Long): String
}
