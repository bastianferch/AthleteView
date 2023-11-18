package ase.athlete_view.domain.activity.pojo.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Activity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?
)
