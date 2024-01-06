package ase.athlete_view.domain.csp.persistence

import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.csp.pojo.entity.CspJob
import ase.athlete_view.domain.user.pojo.entity.Trainer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface CspRepository : JpaRepository<CspJob, Long> {

    @Query("SELECT j FROM CspJob j WHERE j.date = :date AND j.trainer = :trainer")
    fun findJobByTrainerAndDate(
            @Param("trainer") trainer: Trainer,
            @Param("date") date: String
    ): List<CspJob>
}
