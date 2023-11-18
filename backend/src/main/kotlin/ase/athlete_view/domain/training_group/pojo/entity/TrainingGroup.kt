package ase.athlete_view.domain.training_group.pojo.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class TrainingGroup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?
)
