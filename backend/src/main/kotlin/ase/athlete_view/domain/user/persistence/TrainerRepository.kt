package ase.athlete_view.domain.user.persistence

import ase.athlete_view.domain.user.pojo.entity.Trainer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TrainerRepository : JpaRepository<Trainer, Long> {
    fun getTrainerByCode(code: String): Trainer?
}
