package ase.athlete_view.domain.activity.pojo.dto

import jakarta.persistence.Id
import java.io.InputStream

class FitData(
    @Id
    var id: String?,
    var stream: InputStream
)