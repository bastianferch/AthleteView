package ase.athlete_view.domain.training_group.persistence

import ase.athlete_view.domain.training_group.pojo.entity.TrainingGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TrainingGroupRepository : JpaRepository<TrainingGroup, Long> {
}
